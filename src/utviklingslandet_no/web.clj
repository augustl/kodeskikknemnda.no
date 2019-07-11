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
  {"/" "Bør vi lage en web egentlig? Vi er ikke sikre, derfor har du fått denne stygge redirecten til backblaze sin b2 bucket."
   "/rss.xml" (rss/generate-rss (-> "episodes.edn" clojure.java.io/resource slurp clojure.edn/read-string))})

(defn get-assets []
  (assets/load-assets "public" [#".*"]))

(defn wrap-content-type-utf-8 [handler]
  (fn [request]
    (when-let [response (handler request)]
      (if (.contains (get-in response [:headers "Content-Type"]) ";")
        response
        (if (string? (:body response))
          (update-in response [:headers "Content-Type"] #(str % "; charset=utf-8"))
          response)))))

(def app (-> (stasis/serve-pages get-pages)
             (optimus/wrap get-assets optimizations/none strategies/serve-live-assets)
             wrap-content-type
             wrap-content-type-utf-8
             prone/wrap-exceptions))
