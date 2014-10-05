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

(defmacro with-each [a e & body]
  `(let [len# (alength ~a)]
     (loop [i# 0]
       (if (< i# len#)
         (let [~e (aget ~a i#)]
           ~@body
           (recur (inc i#)))))))

(defmacro timez [message & body]
  `(let [start# (-> (js/Date.) (.getTime))]
     ~@body
     (let [end# (-> (js/Date.) (.getTime))]
       (~'js/console.log "time:" ~message (- end# start#)))))
