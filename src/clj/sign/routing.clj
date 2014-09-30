(ns sign.routing
  (:require [clojure.java.io :as io]
            [ring.util.http-response :refer [ok content-type bad-request!] :as resp]
            [compojure.core :refer [context]]
            [compojure.route :as route]
            [compojure.api.core :refer [defroutes* middlewares GET* POST*]]
            [compojure.api.middleware :refer [api-middleware]]
            [sign.env :as env]
            [sign.index-page :refer [index-page]])
  (:import [java.io InputStream ByteArrayInputStream ByteArrayOutputStream]
           [org.apache.commons.codec.binary Base64]))

(defn ->is ^InputStream [^bytes data]
  (ByteArrayInputStream. data))

(def prefix "data:image/png;base64,")

(defn ->image ^bytes [^String image]
  (if-not (.startsWith image prefix)
    (bad-request! {:message "Bad image data"}))
  (Base64/decodeBase64 (.substring image (.length prefix))))

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
        (if-let [image @sign]
          (-> image
              (->is)
              (ok)
              (content-type "image/png"))
          {:status 404
           :body "No image"}))
      (POST* "/sign" [image paths]
        (println paths)
        (reset! sign (->image image))
        (ok {}))
      (POST* "/error" {body :body-params addr :remote-addr {:strs [user-agent]} :headers}
        (println "client error:" body addr user-agent)
        (ok {}))))
  (route/resources "/")
  (constantly (resp/not-found "Not found")))
