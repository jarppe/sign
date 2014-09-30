(ns sign.ui.view.navbar
  (:require [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]))

(defcomponent navbar [app owner]
  (render [_]
    (html
      [:div.navbar.navbar-inverse.navbar-fixed-top
       [:div.container
        [:div.navbar-header
         [:a.navbar-brand {:href "#/"} "sign"]]]])))
