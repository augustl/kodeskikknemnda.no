(ns utviklingslandet-no.cli
  (:require [utviklingslandet-no.web :as web]
            [stasis.core :as stasis]
            [optimus.optimizations :as optimizations]
            [optimus.export]))

(def optimize optimizations/all)

(defn build-site [dir]
  (prn "Get assets")
  (let [assets (optimize (web/get-assets) {})]
    (stasis/empty-directory! dir)
    (prn "Save assets")
    (optimus.export/save-assets assets dir)
    (prn "Export pages")
    (stasis/export-pages (web/get-pages) dir {:optimus-assets assets
                                              :base-url "https://utviklingslandet.no"})
    (prn "Done!")))
