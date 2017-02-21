(ns retro-fever.input.mouse
  (:refer-clojure :exclude [update])
  (:require [clojure.set :refer [difference]]))

(def buttons
  {:left 0
   :middle 1
   :right 2})

(def buttons-pressed (atom #{}))
(def buttons-previous-pressed (atom #{}))
(def buttons-clicked (atom #{}))
(def mouse-position (atom {:x -1 :y -1}))

(defn- calculate-canvas-position [event]
  (let [canvas event.currentTarget
        bounding-box (.getBoundingClientRect canvas)
        canvas-ratio (/ canvas.width bounding-box.width)
        x (Math/round (* (- event.clientX bounding-box.left) canvas-ratio))
        y (Math/round (* (- event.clientY bounding-box.top) canvas-ratio))]
    (when (and (>= x 0) (< x canvas.width)
             (>= y 0) (< y canvas.height))
      {:x x :y y})))

(defn- on-button [f]
  (fn [event]
    (swap! buttons-pressed f event.button)
    (.preventDefault event)
    (.stopPropagation event)))

(defn- on-move [event]
  (when-let [position (calculate-canvas-position event)]
    (reset! mouse-position position))
  (.preventDefault event)
  (.stopPropagation event))

(defn button [key]
  (get buttons key))

(defn button-pressed? [button]
  (contains? @buttons-pressed button))

(defn button-clicked? [button]
  (contains? @buttons-clicked button))

(defn get-position []
  @mouse-position)

;; Check performance impact with/without onmousemove event, maybe make it optional
(defn ^:export init
  "Initializes mouse as input device"
  [canvas & options]
  (aset canvas "onmousedown" (on-button conj))
  (aset canvas "onmouseup" (on-button disj))
  (aset canvas "onmousemove" on-move)
  (aset canvas "oncontextmenu" (fn [] false))) ;; Disable context menu on canvas

(defn update
  "Update clicked states once per update loop"
  []
  (reset! buttons-clicked (difference @buttons-previous-pressed @buttons-pressed))
  (reset! buttons-previous-pressed @buttons-pressed))
