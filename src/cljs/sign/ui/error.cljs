(ns sign.ui.error
  (:require [ajax.core :refer [POST]]))

(def msg-id (atom 0))

(defn error [message]
  (POST "/api/error" {:params (assoc message :msg-id (swap! msg-id inc))}))
