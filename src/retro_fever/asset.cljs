(ns retro-fever.asset
  (:require [clojure.walk :as walk]
            [retro-fever.graphic :as graphic]
            [retro-fever.util :as util]))

; Map to hold all the loaded game assets
(def asset-store (atom {}))

; Vector of assets with dependencies to other assets
(def dependent-assets (atom []))

(defn- image-loaded [id]
  "Callback function for loaded images"
  (let [image (get-in @asset-store id)]
    (swap! asset-store update-in id merge (graphic/get-image-dimensions (:image image)) {:loaded true})))

(defn ^:export load-image
  "Load image from resource"
  ([id src]
   (load-image {:id id :src src}))
  ([{:keys [id src]}]
   (let [id (util/add-id-prefix :images id)
         image (graphic/image src)]
     (swap! asset-store assoc-in id (assoc (graphic/Image. image) :loaded false))
     (.addEventListener image "load" #(image-loaded id)))))

(defn- spritesheet-loaded [id]
  "Callback function for loaded spritesheets"
  (let [{:keys [cols rows] :as spritesheet} (get-in @asset-store id)
        {:keys [width height] :as image-size} (graphic/get-image-dimensions (:image spritesheet))]
    (swap! asset-store update-in id merge image-size
           {:cell-width (/ width cols) :cell-height (/ height rows) :loaded true})))

(defn ^:export load-spritesheet
  "Load spritesheet from source"
  ([id src cols rows]
   (load-spritesheet {:id id :src src :cols cols :rows rows}))
  ([{:keys [id src cols rows]}]
   (let [id (util/add-id-prefix :spritesheets id)
         image (graphic/image src)]
     (swap! asset-store assoc-in id (assoc (graphic/Spritesheet. image cols rows) :loaded false))
     (.addEventListener image "load" #(spritesheet-loaded id)))))

(defn ^:export load-animation
  "Load animation from source or spritesheet entry"
  ([id src]
   (load-animation {:id id :src src}))
  ([id src cycle]
   (load-animation {:id id :src src :cycle cycle}))
  ([id src cycle interval]
   (load-animation {:id id :src src :cycle cycle :interval interval}))
  ([id src cycle interval repeat]
   (load-animation {:id id :src src :cycle cycle :interval interval :repeat repeat}))
  ([id src cycle interval repeat running]
   (load-animation {:id id :src src :cycle cycle :interval interval
                    :repeat repeat :running running}))
  ([{:keys [id src cycle interval repeat running] :as animation}]
   (when (string? src)
     (load-spritesheet animation))
   (swap! dependent-assets conj
          {:type :animation
           :id (util/add-id-prefix :animations id)
           :dependency-id (util/add-id-prefix :spritesheets (if (string? src) id src))
           :options [cycle (or interval 20) (or repeat true) 0 0 (or running false)]})))

(defn- create-by-type [type options]
  "Helper function to call the right constructor for dependent assets"
  (apply (condp = type
           :animation graphic/animation)
         options))

(defn load-dependencies []
  "Runs through all assets with dependencies and adds them to the asset store"
  (doseq [{:keys [id dependency-id type options]} @dependent-assets]
    (let [image (get-in @asset-store dependency-id)]
      (swap! asset-store assoc-in id
             (assoc (create-by-type type (concat [image] options)) :loaded true)))))

(defn- collapse
  "Helper function to check wheter all resources have been loaded"
  [data & [key sub-key]]
  (if (map? data)
    (keep (fn [[k v]]
            (collapse v k key)) data)
    (when (= key :loaded)
      (hash-map sub-key data))))

(defn resources-loaded? []
  "Checks wheter all asynchronous loaded resources have completed"
  (every? true? (vals (apply merge (flatten (collapse @asset-store))))))

(defn load
  "Load assets based on a specification map"
  [spec]
  (doseq [[k f] {:images load-image
                 :spritesheets load-spritesheet
                 :animations load-animation}]
    (doall (map #(f %) (k spec)))))

(defn- get-from-store
  "Internal function to extrac assets from the asset store"
  [id]
  (get-in @asset-store id))

(defn get-image
  "Extracts a given image from the asset store"
  [id]
  (get-from-store (util/add-id-prefix :images id)))

(defn get-spritesheet
  "Extracts a given image from the asset store"
  [id]
  (get-from-store (util/add-id-prefix :spritesheets id)))

(defn get-animation
  "Extracts a given animation from the asset store"
  [id]
  (get-from-store (util/add-id-prefix :animations id)))
