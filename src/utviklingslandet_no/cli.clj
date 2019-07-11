(ns utviklingslandet-no.cli
  (:require [utviklingslandet-no.web :as web]
            [stasis.core :as stasis]
            [optimus.optimizations :as optimizations]
            [optimus.exportl]))

(def optimize optimizations/all)

(defn build-site [dir]
  (let [assets (optimize (web/get-assets) {})]
    (stasis/empty-directory! dir)
    (optimus.export/save-assets assets dir)
    (stasis/export-pages (web/get-pages) dir {:optimus-assets assets
                                              :base-url "https://utviklingslandet.no"})))
