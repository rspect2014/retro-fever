(ns retro-fever.scene
  (:refer-clojure :exclude [update remove])
  (:require [clojure.walk :as walk]
            [retro-fever.sprite :as sprite]
            [retro-fever.util :as util]))

(defprotocol INode
  (add-child-id [this id])
  (get-children-ids [this])
  (remove-child-id [this id]))

(defprotocol IScene
  (add [this element] [this id element])
  (add-to [this destination-id element] [this destination-id id element])
  (remove [this id])
  (move-to [this id destination-id])
  (get-root [this])
  (get-element [this element-id])
  (get-elements [this element-ids])
  (get-elements-at [this node-id]))

(defrecord Node [children]
  INode
  (add-child-id [this id]
    (update-in this [:children] conj id))
  (get-children-ids [this]
    (:children this))
  (remove-child-id [this id]
    (update-in this [:children] disj id)))

(defrecord Scene []
  IScene
  (add [this element]
    (add this (keyword (gensym "se-")) element))
  (add [this id element]
    (add-to this :se-root id element))
  (add-to [this destination-id element]
    (add-to this destination-id (keyword (gensym "se-")) element))
  (add-to [this destination-id id element]
    (-> this
        (assoc id (with-meta element {:se-id id :parent destination-id}))
        (update-in [destination-id] add-child-id id)))
  (remove [this id]
    (let [element (get this id)
          scene (if (= (type element) Node)
                  (reduce #(remove %1 %2) this (get-children-ids element))
                  this)]
      (-> scene
        (update-in [(:parent (meta element))] remove-child-id id)
        (dissoc id))))
  (move-to [this id destination-id]
    (-> this
        (update-in [(:parent (meta (id this)))] remove-child-id id)
        (update-in [id] with-meta (assoc (meta (get-element this id)) :parent destination-id))
        (update-in [destination-id] add-child-id id)))
  (get-root [this]
    (:se-root this))
  (get-element [this element-id]
    (get this element-id))
  (get-elements [this element-ids]
    (map #(get-element this %) element-ids))
  (get-elements-at [this node-id]
    (get-elements this (get-children-ids (get-element this node-id)))))

(defn get-id [element]
  (:se-id (meta element)))

(defn group
  ([]
    (Node. #{}))
  ([update-fn]
     (assoc (group) :update-fn update-fn)))

(defn layer
  ([order]
    (layer order nil))
  ([order update-fn]
     (assoc (group update-fn) :order order)))

(defn scene []
  (assoc (Scene.) :se-root (with-meta (group)  {:se-id :se-root})))

(derive sprite/ImageSprite ::sprite)
(derive sprite/SpritesheetSprite ::sprite)
(derive sprite/AnimatedSprite ::sprite)

(defmulti render type)

(defn render-node [node scene context]
  (doall (map #(if (= (type %) Node)
                 (render-node % scene context)
                 (render % context))
              (sort-by :order (get-elements scene (get-children-ids node))))))

(defmethod render Scene
  [scene context]
  (render-node (get-root scene) scene context))

(defmethod render ::sprite
  [element context]
  (sprite/render element context))

(defmethod render sprite/Image
  [element context]
  (sprite/render-image context element 0 0))

(defmethod render nil
  [element context]
  nil)

(defmethod render :default
  [element context]
  (throw "Unknown render element"))

(defmulti update type)

(defmethod update Scene
  [scene]
  (update (get-root scene) scene))

(defmethod update Node
  [node scene]
  (reduce #(update %2 %1)
    (if-let [update-fn (:update-fn node)]
      (update-fn node scene)
      scene)
    (sort-by :order (get-elements scene (get-children-ids node)))))

(defmethod update ::sprite
  [sprite scene]
  (sprite/update sprite scene))

(defmethod update :default
  [element scene]
  scene)
