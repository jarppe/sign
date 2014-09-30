(ns sign.env)

(def mode (-> (System/getProperty "mode") (or "dev") (keyword)) )

(if-not (#{:dev :prod} mode)
  (throw (ex-info (format "Illegal mode: [%s]" mode) {:mode mode})))

(def dev?  (= mode :dev))
(def prod? (= mode :prod))
