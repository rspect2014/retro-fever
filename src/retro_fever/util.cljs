(ns retro-fever.util)

(defn current-time-ms []
  "Returns the current time in ms"
  (.getTime (js/Date.)))

(defn- add-id-prefix [prefix id]
  "Helper function to add prefix to a id selector"
  (if (vector? id) (concat [prefix] id) [prefix id]))
