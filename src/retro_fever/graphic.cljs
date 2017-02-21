(ns retro-fever.graphic
  (:refer-clojure :exclude [update]))

(defprotocol TypeInfo
  (get-type [this]))

(defn ^:export image
  "Load image from source"
  [src]
  (let [image (js/Image.)]
    (set! (.-src image) src)
    image))

(defn get-image-dimensions [image]
  "Returns map with dimensions of given image"
  (hash-map :width (aget image "width")
            :height (aget image "height")))

(defrecord Image [image]
  TypeInfo
  (get-type [this] :image))

(defrecord Spritesheet [image cols rows]
  TypeInfo
  (get-type [this] :spritesheet))

(defprotocol AnimationActions
  (update-frame [this])
  (get-cell [this])
  (set-running [this run])
  (reset [this]))

(defrecord Animation [spritesheet cycle interval repeat current-idx counter running]
  TypeInfo
  (get-type [this] :animation)
  AnimationActions
  (update-frame [this] (if-not running
                         this
                         (let [c (inc counter)]
                           (if (< c interval)
                             (assoc this :counter c)
                             (merge (assoc this :counter 0)
                                    (let [next-idx (inc current-idx)]
                                      (if (>= next-idx (count cycle))
                                        (if repeat
                                          {:current-idx 0}
                                          {:running false})
                                        {:current-idx next-idx})))))))
  (get-cell [this] (nth cycle current-idx))
  (set-running [this run] (assoc this :running run))
  (reset [this] (assoc this :counter 0 :current-idx 0)))

(defn animation
  "Wrapper function for dynamic creating from other namespaces"
  [spritesheet cycle interval repeat current-idx counter running]
  (Animation. spritesheet cycle interval repeat current-idx counter running))

(defn ^:export render-image
  "Render given image at specified location"
  ([context {:keys [image width height] :as img} x y]
   (render-image context image x y width height 0 0 width height 0 0))
  ([context image x y width height]
   (render-image context image x y width height 0 0 width height 0 0))
  ([context image x y width height sx sy swidth sheight rel-x rel-y]
   (doto context
     (.save)
     (.translate x y)
     (.drawImage image sx sy swidth sheight rel-x rel-y width height)
     (.restore))))

(defn render-frame
  "Render the given frame from the specified spritesheet"
  ([context {:keys [cols] :as spritesheet} cell x y]
   (render-frame context spritesheet (mod cell cols) (int (/ cell cols)) x y))
  ([context {:keys [image cell-width cell-height] :as spritesheet} col row x y]
   (render-image context image x y cell-width cell-height (* col cell-width)
                 (* row cell-height) cell-width cell-height (* (/ cell-width 2) -1)
                 (* (/ cell-height 2) -1))))
