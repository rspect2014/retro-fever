(ns retro-fever.resource.audio
  (:refer-clojure :exclude [update]))

(defn ^:export audio
  "Load audio from source"
  [src]
  (let [audio (js/Audio.)]
    (set! (.-src audio) src)
    audio))

(defprotocol AudioActions
  (start [this])
  (pause [this])
  (stop [this]))

(defrecord Audio
    [audio loop]
  AudioActions
  (start [this]
    (when loop
      (set! (.-loop audio) true))
    (.play audio))
  (pause [this]
    (.pause audio))
  (stop [this]
    (.pause this)
    (set! (.-currentTime audio) 0)))
