(ns sign.ui.main
  (:require [sign.ui.root :as root]
            [sign.ui.error :refer [error]]))

(-> js/window .-onerror (set! (fn [message url line]
                                (error {:type     "on-error"
                                        :message  message
                                        :url      url
                                        :line     line})
                                false)))

(-> js/window .-onload (set! root/start))
