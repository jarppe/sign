(ns sign.main
  (:gen-class))

(defn -main [& args]
  (println "Loading sign...")
  (require 'sign.server)
  (println "Starting sign...")
  ((resolve 'sign.server/start-server) (apply hash-map args)))
