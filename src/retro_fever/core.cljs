(ns retro-fever.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! put! alts! chan timeout]]
            [retro-fever.util :as util]
            [retro-fever.asset :as asset]
            [retro-fever.stats :as stats]
            [retro-fever.scene :as scene]))

                                        ; Atom for holding engine specific values
(def app (atom {:game {:canvas nil :loop nil}}))

(defn init-canvas [id width height]
  "Initialize the game canvas to the canvas with given the id
and set width and height"
  (let [canvas (.getElementById js/document (name id))]
    (set! (.-width canvas) width)
    (set! (.-height canvas) height)
    (swap! app assoc-in [:game :canvas] {:context (.getContext canvas "2d")
                                         :width width
                                         :height height})))

(defn- update-loop [tick-interval update-fn]
  "Internal function to create the update loop logic"
  (fn [next-tick]
    (loop [tick next-tick skips 0]
      (if (and (< tick (util/current-time-ms)) (< skips 5))
        (do (update-fn)
            (stats/record-update)
            (recur (+ tick tick-interval) (inc skips)))
        tick))))

(defn game-loop
  "Main function for the engine game loop, calling update, render and
  collecting statistics from the game loop"
  [tick-interval update-fn render-fn]
  (let [quit-chan (chan)
        update (update-loop tick-interval update-fn)]
    (go (loop [next-tick (util/current-time-ms)]
          (let [[v ch] (alts! [quit-chan (timeout (- next-tick (util/current-time-ms)))])]
            (when-not (= ch quit-chan)
              (stats/record-start)
              (let [tick (update next-tick)
                    {:keys [context width height]} (get-in @app [:game :canvas])]
                (.clearRect context 0 0 width height)
                (render-fn context)
                (stats/record-render)
                (stats/calculate)
                (recur tick))))))
    (swap! app assoc-in [:game :loop] quit-chan)))

(defn stop-loop []
  "Stops the current game loop"
  (put! (get-in @app [:game :loop]) false))

(defn setup [& fns]
  "Wrapper function to ensure resources are loaded before they are used"
  (go (loop []
        (if (asset/resources-loaded?)
          (do (asset/load-dependencies)
              (doseq [f fns]
                (f)))
          (do (<! (timeout 100))
              (recur))))))

(defmulti game type)

(defmethod game js/Function
  [update-fn render-fn & options]
  (let [options-map (reduce (fn [r [k v]] (assoc r k v))
                            {} (partition 2 options))
        opts (merge {:ups 60}
                    options-map)]
    (game-loop (/ 1000 (:ups opts))
               update-fn render-fn)))

(defmethod game cljs.core/Atom
  [game-state & options]
  (let [scene-view (if (vector? (first options)) (first options) nil)
        options (if scene-view (rest options) options)]
    (game
     (if (empty? scene-view)
       (fn [] (swap! game-state scene/update))
       (fn [] (swap! game-state update-in scene-view scene/update)))
     (fn [context] (scene/render (get-in @game-state scene-view) context))
     options)))
