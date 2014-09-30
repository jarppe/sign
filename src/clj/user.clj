(ns user)

(defn init []
  (require '[sign.server :as server]
           '[sign.env :as env]))

(defn run []
  (println "Loading sign...")
  (require 'sign.server)
  (println "Starting sign...")
  ((resolve 'sign.server/start-server) {"http" "8080", "nrepl" "6000"}))

"Server ready"
