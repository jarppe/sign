(ns sign.routing
  (:require [clojure.java.io :as io]
            [ring.util.http-response :refer [ok content-type bad-request!] :as resp]
            [compojure.core :refer [context]]
            [compojure.route :as route]
            [compojure.api.core :refer [defroutes* middlewares GET* POST*]]
            [compojure.api.middleware :refer [api-middleware]]
            [sign.env :as env]
            [sign.index-page :refer [index-page]]
            [sign.image :refer [->image]]))

(def sign (atom nil))

(defroutes* routes
  (GET* "/" []
    (-> index-page
        (ok)
        (content-type "text/html; charset=\"UTF-8\"")))
  (GET* "/react.js" []
    (-> "react/react.js"
        (io/resource)
        (io/input-stream)
        (ok)
        (content-type "application/javascript; charset=\"UTF-8\"")))
  (middlewares [(api-middleware)]
    (context "/api" []
      (GET* "/sign" []
        :query-params [{width  :- Long 600}
                       {height :- Long 400}]
        (-> @sign
            (->image width height)
            (ok)
            (content-type "image/png")))
      (POST* "/sign" [paths]
        (reset! sign paths)
        (ok {}))
      (POST* "/error" {body :body-params addr :remote-addr {:strs [user-agent]} :headers}
        (println "client error:" body addr user-agent)
        (ok {}))))
  (route/resources "/")
  (constantly (resp/not-found "Not found")))
