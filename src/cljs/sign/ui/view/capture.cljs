(ns sign.ui.view.capture
  (:require [clojure.string :as s]
            [cljs.core.async :as a :refer [<! put!]]
            [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]
            [ajax.core :refer [GET POST]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [sign.ui.util :refer [with-each]]))

;;
;; Common utils:
;;

(defn- pd [e]
  (.preventDefault e)
  (.stopPropagation e)
  e)

;;
;; Mouse events:
;;

(defn- on-mousedown [<events]
  (fn [e]
    (pd e)
    (put! <events [:start (.-clientX e) (.-clientY e)])))

(defn- on-mousemove [<events]
  (fn [e]
    (pd e)
    (put! <events [:move (.-clientX e) (.-clientY e)])))

(defn- on-mouseup [<events] 
  (fn [e]
    (pd e)
    (put! <events [:end])))

;;
;; Touch events:
;;

(defn- on-touchstart [<events]
  (fn [e]
    (pd e)
    (let [touch (-> e .-touches (aget 0))]
      (put! <events [:start (.-clientX touch) (.-clientY touch)]))))

(defn- on-touchmove [<events]
  (fn [e]
    (pd e)
    (let [touch (-> e .-touches (aget 0))]
      (put! <events [:move (.-clientX touch) (.-clientY touch)]))))

(defn- on-touchend [<events]
  (fn [e]
    (pd e)
    (put! <events [:end])))

;;
;; Process events into a paths:
;;

(defn- new-current [paths x y]
  (let [current (array [x y])]
    (.push paths current)
    current))

(defn- append [current x y]
  (if current
    (.push current [x y]))
  current)

(defn- events->paths [<events paths [rx ry]]
  (go-loop [current nil]
    (let [[event x y] (<! <events)]
      (condp = event
        :start  (recur (new-current paths (- x rx) (- y ry)))
        :move   (recur (append current (- x rx) (- y ry)))
        :end    (recur nil)
        nil))))

;;
;; Scheduler:
;;

(def schedule (or (.-requestAnimationFrame js/window)
                  (.-mozRequestAnimationFrame js/window)
                  (.-webkitRequestAnimationFrame js/window)
                  (.-msRequestAnimationFrame js/window)
                  (fn [f] (.setTimeout js/window f 16))))

;;
;; Render paths:
;;

(defn- render-path [ctx path]
  (when (-> path alength pos?)
    (.beginPath ctx)
    (let [[x y] (aget path 0)]
      (.moveTo ctx x y))
    (with-each path [x y]
      (.lineTo ctx x y))
    (.stroke ctx)))

(defn- render [ctx paths run?]
  (schedule (fn renderer []
              (let [max-i (-> paths alength dec)]
                (if (>= max-i 0)
                  (loop [i 0]
                    (render-path ctx (aget paths i))
                    (if (< i max-i)
                      (recur (inc i))))))
              (if @run?
                (schedule renderer)))))

;;
;; Init canvas:
;;

(defn- init-canvas [canvas ctx <events]
  (let [width    (.-offsetWidth canvas)
        height   (.-offsetHeight canvas)]
    (doto canvas
      (aset "width" width)
      (aset "height" height))
    (doto ctx
      (aset "fillStyle" "rgb(192,192,192)")
      (.fillRect 0 0 width height)
      (aset "strokeStyle" "rgb(255,255,255)")
      (aset "lineWidth" 2.0)
      (.beginPath)
      (.moveTo 60 (- height 60))
      (.lineTo (- width 60) (- height 60))
      (.stroke)
      (aset "strokeStyle" "rgb(0,0,0)")
      (aset "fillStyle" "rgb(0,0,0)")
      (aset "lineWidth" 5.0))
    (doto canvas
      (aset "onmousedown"   (on-mousedown  <events))
      (aset "onmousemove"   (on-mousemove  <events))
      (aset "onmouseup"     (on-mouseup    <events))
      (aset "ontouchstart"  (on-touchstart <events))
      (aset "ontouchmove"   (on-touchmove  <events))
      (aset "ontouchend"    (on-touchend   <events))
      (aset "ontouchcancel" (on-touchend   <events)))))

;;
;; :
;;

(defn- canvas-offsets [canvas]
  (let [rect (.getBoundingClientRect canvas)
        rx   (.-left rect)
        ry   (.-top rect)]
    [rx ry]))

(defn- init-sign-context [canvas paths]
  (let [<events  (a/chan (a/sliding-buffer 32))
        <close   (a/chan)
        run?     (atom true)
        ctx      (.getContext canvas "2d")]
    (init-canvas canvas ctx <events)
    (events->paths <events paths (canvas-offsets canvas))
    (render ctx paths run?)
    (go
      (<! <close)
      (reset! run? false)
      (a/close! <events))
    <close))

;;
;; Capture signature components:
;;

(defcomponent capture [app owner {:keys [<toggle]}]
  (init-state [_]
    {:paths (array)})
  (did-mount [_]
     (let [<close (init-sign-context
                    (om/get-node owner "sign-canvas")
                    (om/get-state owner :paths))]
       (om/set-state! owner :<close <close)))
  (will-unmount [_]
     (a/close! (om/get-state owner :<close)))
  (render-state [_ {:keys [paths]}]
    (html
      [:div
       [:canvas.sign.capturing {:ref "sign-canvas"}]
       [:button.btn.btn-success.w120
        {:on-click (fn [_]
                     (POST "/api/sign"
                       {:params {:paths (js->clj paths)}
                        :handler (fn [_] (put! <toggle true))}))}
        "Save"]])))
