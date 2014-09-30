(ns sign.ui.view.sign
  (:require [clojure.string :as s]
            [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]))

(defcomponent sign [app owner]
  (render [_]
    (html
      [:div
       [:h1 "Sign"]
       [:button.btn.btn-default {:on-click (fn [_] (om/update! app :sign? false))} "Exit"]])))
