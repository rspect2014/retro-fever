(defproject retro-fever "0.2.0"
  :description "A 2D game engine targeting modern browsers and mobile devices"
  :url "http://rf.clojurecup.com"
  :scm {:name "git"
        :url "https://github.com/rspect2014/retro-fever.git"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2511"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]]
  :plugins [[lein-cljsbuild "1.0.3"]]
  :cljsbuild {
    :builds [{
        :source-paths ["src"]
        :compiler {
          :output-to "target/retro-fever.js"
          :optimizations :whitespace
          :pretty-print true}}]}
)
