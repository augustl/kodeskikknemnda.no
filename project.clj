(defproject utviklingslandet-no "0.1.0-SNAPSHOT"
  :description "Statisk generering av utviklingslandet.no"
  :url "https://utviklingslandet.no"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [optimus "0.20.2"]
                 [optimus-img-transform "0.3.1"]
                 [stasis "2.4.0" :exclusions [org.clojure/clojure]]
                 [ring "1.7.1"]
                 [hiccup "1.0.5"]
                 [prone "2019-07-08"]]
  :jvm-opts ["-Xmx768M"
             "-Djava.awt.headless=true"]
  :ring {:handler utviklingslandet-no.web/app
         :port 3344}
  :aliases {"build-site" ["run" "-m" "utviklingslandet-no.cli/build-site" "dist"]}
  :profiles {:dev {:dependencies [[clj-tagsoup/clj-tagsoup "0.3.0" :exclusions [org.clojure/clojure]]
                                  [hiccup-find  "1.0.0"]]
                   :plugins [[lein-ring "0.12.5"]]
                   :source-paths ["dev" "config"]}})

