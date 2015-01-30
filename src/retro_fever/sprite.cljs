(ns retro-fever.sprite
  (:refer-clojure :exclude [update])
  (:require [retro-fever.graphic :as graphic]
            [retro-fever.util :as util]))

(defprotocol TypeInfo
  (get-type [this]))

(defn ^:export move [{:keys [x y velocity-x velocity-y] :as sprite}]
  "Moves a sprite based on its velocity on the x and y-axis"
  (assoc sprite
         :x (+ x velocity-x)
         :y (+ y velocity-y)))

(defn- update-sprite
  [& [sprite scene :as args]]
  (if-let [update-fn (:update-fn sprite)]
    (apply update-fn args)
    (let [updated-sprite (move sprite)]
      (if scene
        (assoc-in scene [(:se-id (meta sprite))] updated-sprite)
        updated-sprite))))

(defn- update-animated-sprite
  ([sprite]
   (update-animated-sprite sprite nil))
  ([sprite scene]
   (update-sprite (update-in sprite [:animation] graphic/update-frame
                             (:animation sprite)) scene)))

(defprotocol SpriteActions
  (render [this context])
  (update [this] [this scene]))

(defrecord StaticImage [image width height x y]
  TypeInfo
  (get-type [this] :static-image)
  SpriteActions
  (render [this context] (graphic/render-image context (:image image) x y width height))
  (update [this] this)
  (update [this scene] scene))

(defn ^:export static-image
  ([{:keys [width height] :as image} x y]
   (static-image image width height x y))
  ([image width height x y]
   (StaticImage. image width height x y)))

(defrecord ImageSprite [image width height x y velocity-x velocity-y]
  TypeInfo
  (get-type [this] :image-sprite)
  SpriteActions
  (render [this context] (graphic/render-image context (:image image) x y width height 0 0 width height
                                               (* (/ width 2) -1) (* (/ height 2) -1)))
  (update [this] (update-sprite this))
  (update [this scene] (update-sprite this scene)))

(defn image-sprite
  "Creates a sprite represented by the given image with the specified values"
  ([{:keys [image] :as data}]
   (let [{:keys [width height]} image]
     (merge (ImageSprite. image width height 0 0 0 0)
            data)))
  ([{:keys [width height] :as image} x y]
   (image-sprite image width image x y 0 0))
  ([image width height x y]
   (image-sprite image width height x y 0 0))
  ([image width height x y velocity-x velocity-y]
   (ImageSprite. image width height x y velocity-x velocity-y)))

(defrecord SpritesheetSprite [spritesheet cell width height x y velocity-x velocity-y]
  TypeInfo
  (get-type [this] :spritesheet-sprite)
  SpriteActions
  (render [this context] (graphic/render-frame context spritesheet cell x y))
  (update [this] (update-sprite this))
  (update [this scene] (update-sprite this scene)))

(defn spritesheet-sprite
  ([{:keys [spritesheet] :as data}]
   (let [{:keys [cell-width cell-height]} spritesheet]
     (merge (SpritesheetSprite. spritesheet 0 cell-width cell-height 0 0 0 0)
            data)))
  ([{:keys [cell-width cell-height] :as spritesheet} cell x y]
   (spritesheet-sprite spritesheet cell cell-width cell-height x y 0 0))
  ([spritesheet cell width height x y]
   (spritesheet-sprite spritesheet width height cell x y 0 0))
  ([spritesheet cell width height x y velocity-x velocity-y]
   (SpritesheetSprite. spritesheet width height cell x y velocity-x velocity-y)))

(defrecord AnimatedSprite [animation width height x y velocity-x velocity-y]
  TypeInfo
  (get-type [this] :animated-sprite)
  SpriteActions
  (render [this context] (graphic/render-frame context (:spritesheet animation)
                                               (graphic/get-cell animation) x y))
  (update [this] (update-animated-sprite this))
  (update [this scene] (update-animated-sprite this scene)))

(defn animated-sprite
  ([{:keys [animation] :as data}]
   (let [{:keys [cell-width cell-height]} (:spritesheet animation)]
     (merge (AnimatedSprite. animation cell-width cell-height 0 0 0 0)
            data)))
  ([{:keys [spritesheet] :as animation} x y]
   (let [{:keys [cell-width cell-height]} spritesheet]
     (animated-sprite animation cell-width cell-height x y 0 0)))
  ([animation width height x y]
   (animated-sprite animation width height x y 0 0))
  ([animation width height x y velocity-x velocity-y]
   (AnimatedSprite. animation width height x y velocity-x velocity-y)))

(defn ^:export new
  "Creates a sprite record based on graphics resource"
  [& args]
  (let [arg-1 (first args)
        type (if (and (= (count args) 1) (map? arg-1))
               (some #{:image :spritesheet :animation} (keys (first args)))
               (graphic/get-type arg-1))]
    (condp = type
      :spritesheet (apply spritesheet-sprite args)
      :animation (apply animated-sprite args)
      :image (apply image-sprite args))))

(defn collides?
  "Checks wheter 2 sprites have collided using bounding boxes"
  [s1 s2]
  (and (< (* (Math/abs (- (:x s1) (:x s2))) 2) (+ (:width s1) (:width s2)))
       (< (* (Math/abs (- (:y s1) (:y s2))) 2) (+ (:height s1) (:height s2)))))

(defn distance-to
  "Calculates the distance between the centers of two sprites"
  [s1 s2]
  (let [diff-x (- (:x s1) (:x s2))
        diff-y (- (:y s1) (:y s2))]
    (Math/sqrt (+ (* diff-x diff-x)
                  (* diff-y diff-y)))))
