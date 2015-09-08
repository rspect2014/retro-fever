(defproject retro-fever "0.2.1-SNAPSHOT"
  :description "A 2D game engine targeting modern browsers and mobile devices"
  :url "http://rf.clojurecup.com"

  :scm {:name "git"
        :url "https://github.com/rspect2014/retro-fever.git"}

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.48"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]]

  :plugins [[lein-cljsbuild "1.1.0"]]

  ; Enable the lein hooks for: clean, compile, test, and jar.
  :hooks [leiningen.cljsbuild]

  :clean-targets ^{:protect false} ["resources/retro-fever.js"
                                    "resources/out"]

  :cljsbuild {
    :builds {
      :engine
      {:source-paths ["src"]
       :compiler {
         :output-to "resources/retro-fever.js"
         :output-dir "resources/out"
         :optimizations :none
         :pretty-print true}}}}

  :profiles
  {:dev {:cljsbuild {:builds {:engine {:source-paths ["env/dev"]}}}}
   :prod {:cljsbuild {:builds {:engine {:output-to "target/retro-fever.js"
                                        :output-dir "target/out"
                                        :optimizations :advanced
                                        :pretty-print false}}}}}
)
