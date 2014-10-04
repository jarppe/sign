(ns sign.ui.view.sign
  (:require [clojure.string :as s]
            [cljs.core.async :as a :refer [<! put!]]
            [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]
            [ajax.core :refer [GET POST]]
            [sign.ui.view.capture :refer [capture]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [sign.ui.util :refer [while-let]]))

(defcomponent show [app owner {:keys [<toggle]}]
  (render [_]
    (html
      [:div
       [:img.sign {:src "/api/sign"}]
       [:button.btn.btn-default.w120 {:on-click (fn [_] (put! <toggle true))} "Sign"]])))

(defcomponent sign [app owner]
  (init-state [_]
    {:sign?    true
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
