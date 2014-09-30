(ns sign.ui.root
  (:require [clojure.string :as s]
            [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]
            [sign.ui.app-state :refer [app-state]]
            [sign.ui.view.navbar :refer [navbar]]
            [sign.ui.view.sign :refer [sign]]))

(defcomponent render-root [app owner]
  (render [_]
    (html
      (om/build navbar app)
      (om/build sign app))))

(defn start []
  (om/root render-root app-state {:target (js/document.getElementById "app")}))
