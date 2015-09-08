(defproject rf-input1 "0.1.0-SNAPSHOT"
  :description "Demo of the input module for Retro Fever"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.48"]
                 [retro-fever "0.2.1-SNAPSHOT"]
                 [enfocus "2.1.0"]]
  :plugins [[lein-cljsbuild "1.1.0"]]
  :cljsbuild {
    :builds [{
        :source-paths ["src"]
        :compiler {
          :output-to "resources/rf-input1.js"
          :optimizations :whitespace
          :pretty-print true}}]}
)
