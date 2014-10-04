(ns sign.image
  (:import [java.awt Graphics2D Color BasicStroke RenderingHints]
           [java.awt.image BufferedImage]
           [java.awt.geom Path2D$Float]
           [javax.imageio ImageIO]
           [java.io ByteArrayInputStream ByteArrayOutputStream]))

(set! *warn-on-reflection* true)

(def init-bounds {:minx Long/MAX_VALUE
                  :maxx Long/MIN_VALUE
                  :miny Long/MAX_VALUE
                  :maxy Long/MIN_VALUE})

(defn with [bounds key compare-fn value]
  (if (compare-fn value (key bounds))
    (assoc bounds key value)
    bounds))

(defn bound [bounds [x y]]
  (-> bounds
      (with :minx < x)
      (with :maxx > x)
      (with :miny < y)
      (with :maxy > y)))

(defn scales [paths ^long width ^long height]
  (if (seq paths)
    (let [{:keys [minx maxx miny maxy]} (reduce bound init-bounds (mapcat identity paths))
          sign-w   (- maxx minx)
          sign-h   (- maxy miny)
          scale-x  (double (/ width sign-w))
          scale-y  (double (/ height sign-h))
          scale    (min scale-x scale-y)
          new-w    (* sign-w scale)
          new-h    (* sign-h scale)
          delta-x  (- (/ (- width new-w) 2) minx)
          delta-y  (- (/ (- height new-h) 2) miny)
          trans-x  (fn [x] (-> x (+ delta-x) (* scale)))
          trans-y  (fn [y] (-> y (+ delta-y) (* scale)))]
      [trans-x trans-y scale])))

(defn paint-paths [^Graphics2D g paths [sx sy s]]
  (doseq [p paths]
    (if (-> p count (= 1))
      (let [[x y] (first p)]
        (.fillOval g (sx x) (sy y) (* s 2) (* s 2)))
      (let [path (Path2D$Float.)
            [[x y] & more] p]
        (.moveTo path ^double (sx x) ^double (sy y))
        (doseq [[x y] more]
          (.lineTo path ^double (sx x) ^double (sy y)))
        (.draw g path)))))

(defn- paint [^BufferedImage image paths ^long width ^long height]
  (doto (.createGraphics image)
    (.setRenderingHint RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON)
    (.setPaint Color/WHITE)
    (.fillRect 0 0 width height)
    (.setStroke (BasicStroke. 1.5 BasicStroke/CAP_ROUND BasicStroke/JOIN_ROUND))
    (.setPaint Color/BLACK)
    (paint-paths paths (scales paths width height))
    (.dispose))
  image)

(defn- ->bytes [^BufferedImage image]
  (let [out (ByteArrayOutputStream. 4096)]
    (ImageIO/write image "png" out)
    (-> out
        (.toByteArray)
        (ByteArrayInputStream.))))

(defn ->image [paths ^long width ^long height]
  (-> (BufferedImage. (max width 1) (max height 1) BufferedImage/TYPE_INT_ARGB)
      (paint paths width height)
      (->bytes)))
