(ns retro-fever.input.keyboard
  (:refer-clojure :exclude [update])
  (:require [clojure.set :refer [difference]]
            [retro-fever.input.key-codes :refer [key-codes]]))

(def keys-pressed (atom #{}))
(def keys-previous-pressed (atom #{}))
(def keys-clicked (atom #{}))

(defn- on-key [state-map f]
  (fn [event]
    (swap! state-map f event.keyCode)
    (.preventDefault event)
    (.stopPropagation event)))

(defn key-code [key]
  (get key-codes key))

(defn key-pressed? [key-code]
  (contains? @keys-pressed key-code))

(defn key-clicked? [key-code]
  (contains? @keys-clicked key-code))

(defn ^:export init
  "Initializes keyboard as input device"
  []
  (aset js/window "onkeydown" (on-key keys-pressed conj))
  (aset js/window "onkeyup" (on-key keys-pressed disj)))

(defn update
  "Update clicked states once per update loop"
  []
  (reset! keys-clicked (difference @keys-previous-pressed @keys-pressed))
  (reset! keys-previous-pressed @keys-pressed))
