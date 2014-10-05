(ns sign.ui.util)

(defmacro while-let [[s f] & body]
  `(loop [~s ~f]
     (when ~s
       ~@body
       (recur ~f))))

(defmacro forloop [[init test step] & body]
  `(loop [~@init]
     (when ~test
       ~@body
       (recur ~step))))
