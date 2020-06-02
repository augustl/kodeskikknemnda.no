(ns kodeskikknemnda-no.cli
  (:require [kodeskikknemnda-no.web :as web]
            [stasis.core :as stasis]
            [optimus.optimizations :as optimizations]
            [optimus.export]))

(def optimize optimizations/all)

(defn build-site [dir]
  (println "Get assets")
  (let [assets (optimize (web/get-assets) {})]
    (stasis/empty-directory! dir)
    (println "Save assets")
    (optimus.export/save-assets assets dir)
    (println "Export pages")
    (stasis/export-pages (web/get-pages) dir {:optimus-assets assets
                                              :base-url "https://kodeskikknemnda.no"})
    (println "Done!")))
