(ns sign.index-page
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]
            [sign.env :as env]))

(def index-page
  (html
    (html5
      [:head
       [:title "sign"]
       [:meta {:charset "utf-8"}]
       [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
       [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
       [:link {:href "sign.css" :rel "stylesheet" :type "text/css"}]
       (if env/prod?
         [:script {:src "sign.js" :type "text/javascript" :async "async"}]
         (concat (include-js "out/goog/base.js" "react.js" "sign.js")
                 [[:script {:type "text/javascript"} "goog.require('sign.ui.main');"]]))]
      [:body
       [:div#app
        [:h1 "Loading, please wait..."]]])))
