(defproject rf-basic-game "0.1.0-SNAPSHOT"
  :description "Demoing a basic game using Retro Fever"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.293"]
                 [retro-fever "0.2.1-SNAPSHOT"]]
  :plugins [[lein-cljsbuild "1.1.5"]]
  :cljsbuild {
    :builds [{
        :source-paths ["src"]
        :compiler {
          :output-to "resources/rf-basic-game.js"
          :optimizations :whitespace
          :pretty-print true}}]}
)
