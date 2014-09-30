(ns sign.ui.view.sign
  (:require [clojure.string :as s]
            [cljs.core.async :as a :refer [<! put!]]
            [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]
            [ajax.core :refer [GET POST]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [sign.ui.util :refer [while-let]]))

(defn pd [e]
  (.preventDefault e)
  (.stopPropagation e)
  e)

(defn save-sign [owner <toggle]
  (fn [_]
    (let [canvas (om/get-node owner "sign-canvas")
          image  (.toDataURL canvas)]
      (POST "/api/sign" {:params {:image image}
                         :handler (fn [resp]
                                    (js/console.log "resp:" (pr-str resp))
                                    (put! <toggle true))}))))

(defcomponent capture [app owner {:keys [<toggle]}]
  (init-state [_]
    {:state (atom nil)})
  (did-mount [_]
    (let [state  (om/get-state owner :state)
          canvas (om/get-node owner "sign-canvas")
          rect   (.getBoundingClientRect canvas)
          rx     (.-left rect)
          ry     (.-top rect)
          width  (.-offsetWidth canvas)
          height (.-offsetHeight canvas)
          ctx    (.getContext canvas "2d")]
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
        (aset "onmousedown" (fn [e]
                              (pd e)
                              (let [x (- (.-clientX e) rx)
                                    y (- (.-clientY e) ry)]
                                (reset! state [x y]))))
        (aset "onmouseup" (fn [e]
                            (pd e)
                            (reset! state nil)))
        (aset "onmousemove" (fn [e]
                              (pd e)
                              (if-let [[x y] @state]
                                (let [nx (- (.-clientX e) rx)
                                      ny (- (.-clientY e) ry)
                                      dx (- x nx)
                                      dy (- y ny)
                                      d  (Math/sqrt (+ (* dx dx) (* dy dy)))]
                                  (when (> d 5.0)
                                    (doto ctx
                                      (.beginPath)
                                      (.moveTo x y)
                                      (.lineTo nx ny)
                                      (.stroke))
                                    (reset! state [nx ny])))))))))
  (render-state [_ {:keys [done]}]
    (html
      [:div
       [:canvas.sign.capturing {:ref "sign-canvas"}]
       [:button.btn.btn-success.w120 {:on-click (save-sign owner <toggle)} "Save"]])))

(defcomponent show [app owner {:keys [<toggle]}]
  (init-state [_]
    {})
  (render-state [_ {:keys []}]
    (html
      [:div
       [:img.sign {:src "/api/sign"}]
       [:button.btn.btn-default.w120 {:on-click (fn [_] (put! <toggle true))} "Sign"]])))

(defcomponent sign [app owner]
  (init-state [_]
    {:sign?    false
     :<toggle  (a/chan)})
  (will-mount [_]
    (let [<toggle (om/get-state owner :<toggle)]
      (go
        (while-let [_ (<! <toggle)]
          (om/update-state! owner :sign? not)))))
  (will-unmount [_]
    (a/close! (om/get-state owner :<toggle)))
  (render-state [_ {:keys [sign? <toggle]}]
    (html
      [:div.container
       (if sign?
         (om/build capture app {:opts {:<toggle <toggle}})
         (om/build show app {:opts {:<toggle <toggle}}))])))
