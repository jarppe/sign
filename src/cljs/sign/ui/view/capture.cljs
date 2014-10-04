(ns sign.ui.view.capture
  (:require [clojure.string :as s]
            [cljs.core.async :as a :refer [<! put!]]
            [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]
            [ajax.core :refer [GET POST]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [sign.ui.util :refer [while-let]]))

;;
;; Common utils:
;;

(defn- pd [e]
  (.preventDefault e)
  (.stopPropagation e)
  e)

(defn find-touch [touch-id touches]
  (areduce touches i acc nil
           (or acc (let [touch (aget touches i)]
                     (if (= touch-id (.-identifier touch))
                       touch)))))

(defn- start-path [context x y touch-id]
  (let [{:keys [rx ry]} @context
        x (- x rx)
        y (- y ry)]
    (swap! context assoc
           :x         x
           :y         y
           :path      [[x y]]
           :touch-id  touch-id)))

(defn- append-path [context nx ny]
  (let [{:keys [ctx rx ry x y path]} @context]
    (when (seq path)
      (let [nx (- nx rx)
            ny (- ny ry)
            dx (- x nx)
            dy (- y ny)
            d  (Math/sqrt (+ (* dx dx) (* dy dy)))]
        (when (> d 2.0)
          (doto ctx
            (.beginPath)
            (.moveTo x y)
            (.lineTo nx ny)
            (.stroke))
          (swap! context assoc
                 :x nx
                 :y ny
                 :path (conj path [nx ny])))))))

(defn- end-path [context]
  (let [{:keys [path add!]} @context]
    (add! path)
    (swap! context assoc
           :path      nil
           :touch-id  nil)))

;;
;; Mouse events:
;;

(defn- on-mousedown [context]
  (fn [e]
    (pd e)
    (start-path context (.-clientX e) (.-clientY e) nil)))

(defn- on-mousemove [context]
  (fn [e]
    (pd e)
    (append-path context (.-clientX e) (.-clientY e))))

(defn- on-mouseup [context] 
  (fn [e]
    (pd e)
    (end-path context)))

;;
;; Touch events:
;;

(defn- on-touchstart [context]
  (fn [e]
    (pd e)
    (if (-> @context :touch-id not)
      (let [touch     (aget (.-touches e) 0)
            touch-id  (.-identifier touch)]
        (start-path context (.-clientX touch) (.-clientY touch) touch-id)))))

(defn- on-touchmove [context]
  (fn [e]
    (pd e)
    (if-let [touch (-> @context :touch-id (find-touch (.-touches e)))]
      (append-path context (.-clientX touch) (.-clientY touch)))))

(defn- on-touchend [context]
  (fn [e]
    (pd e)
    (if-not (-> @context :touch-id (find-touch (.-touches e)))
      (end-path context))))

;;
;; Initialize signature context:
;;

(defn- init-sign-context! [canvas add!]
  (let [rect     (.getBoundingClientRect canvas)
        rx       (.-left rect)
        ry       (.-top rect)
        width    (.-offsetWidth canvas)
        height   (.-offsetHeight canvas)
        ctx      (.getContext canvas "2d")
        context  (atom {:add!   add!
                        :ctx    ctx
                        :rx     rx
                        :ry     ry})]
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
      (aset "lineWidth" 5.0))
    (doto canvas
      (aset "onmousedown"   (on-mousedown context))
      (aset "onmousemove"   (on-mousemove context))
      (aset "onmouseup"     (on-mouseup context))
      (aset "ontouchstart"  (on-touchstart context))
      (aset "ontouchmove"   (on-touchmove context))
      (aset "ontouchend"    (on-touchend context))
      (aset "ontouchcancel" (on-touchend context)))))

(defcomponent capture [app owner {:keys [<toggle]}]
  (init-state [_]
    {:paths []})
  (did-mount [_]
    (init-sign-context!
      (om/get-node owner "sign-canvas")
      (fn [p] (om/update-state! owner :paths #(conj % p)))))
  (render-state [_ {:keys [paths]}]
    (html
      [:div
       [:canvas.sign.capturing {:ref "sign-canvas"}]
       [:button.btn.btn-success.w120
        {:disabled (empty? paths)
         :on-click (fn [_]
                     (POST "/api/sign"
                       {:params {:paths paths}
                        :handler (fn [_] (put! <toggle true))}))}
        "Save"]])))
