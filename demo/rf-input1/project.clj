(defproject rf-input1 "0.1.0-SNAPSHOT"
  :description "Demo of the input module for Retro Fever"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.293"]
                 [retro-fever "0.2.1-SNAPSHOT"]
                 [enfocus "2.1.0"]]
  :plugins [[lein-cljsbuild "1.1.5"]]
  :cljsbuild {
    :builds [{
        :source-paths ["src"]
        :compiler {
          :output-to "resources/rf-input1.js"
          :optimizations :whitespace
          :pretty-print true}}]}
)
