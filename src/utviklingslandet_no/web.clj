(ns utviklingslandet-no.web
  (:require [stasis.core :as stasis]
            [optimus.assets :as assets]
            [optimus.prime :as optimus]
            [optimus.strategies :as strategies]
            [optimus.optimizations :as optimizations]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [prone.middleware :as prone]
            [utviklingslandet-no.rss :as rss]))

(defn get-pages []
  {"/" "Hello, world!"
   "/rss.xml" (rss/generate-rss (-> "episodes.edn" clojure.java.io/resource slurp clojure.edn/read-string))})

(defn get-assets []
  (assets/load-assets "public" [#".*"]))

(def app (-> (stasis/serve-pages get-pages)
             (optimus/wrap get-assets optimizations/none strategies/serve-live-assets)
             wrap-content-type
             prone/wrap-exceptions))
