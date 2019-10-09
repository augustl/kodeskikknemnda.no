(ns utviklingslandet-no.web
  (:require [stasis.core :as stasis]
            [optimus.assets :as assets]
            [optimus.prime :as optimus]
            [optimus.strategies :as strategies]
            [optimus.optimizations :as optimizations]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [hiccup.page]
            [prone.middleware :as prone]
            [clojure.edn]
            [utviklingslandet-no.rss :as rss]))

(defn layout [title & body]
  (hiccup.page/html5 {:lang "no"}
    [:head
     [:meta {:charset "utf-8"}]
     [:title title]
     [:link {:type "application/rss+xml" :rel "alternate" :title "utviklingslandet.no" :href "https://utviklingslandet.no/rss.xml"}]
     [:link {:rel "stylesheet" :href "/site.css"}]]
    [:body body]))

(defn get-home-page [episodes]
  (layout
    "Utviklingslandet"

    [:p "Dette er den supre websiden vår."]
    [:p "Har du lyst til å høre på podcast?"]
    [:ul
     [:li "Søk på " [:strong "Utviklingslandet"] " i en podcast-app nær deg."]
     [:li [:a {:href "https://www.youtube.com/channel/UChpu1nP54SaYlFznoecHqKg"} "youtube.com"] " (kanal med alle episodene)"]
     [:li [:a {:href "https://twitter.com/utviklingsland"} "twitter.com/utviklingsland"]]]

    [:h2 "Episoder"]
    (map
      (fn [episode]
        [:div
         [:p [:a {:href (:ep/link episode)} (:ep/title episode)]]])
      (reverse episodes))))

(defn get-episode-page [episode]
  (layout
    (:ep/title episode)

    [:p [:a {:href "/"} "< Tilbake til forsiden"]]
    [:h1 (:ep/title episode)]
    [:p [:strong (:ep/subtitle episode)]]
    [:p (:ep/description episode)]
    [:iframe
     {:width "560"
      :height "315"
      :src (str "https://www.youtube.com/embed/" (:ep/youtube-id episode))
      :frameborder "0"
      :allow "accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture"
      :allowfullscreen "true"}]))

(defn get-pages []
  (let [{:keys [episodes]} (-> "episodes.edn" clojure.java.io/resource slurp clojure.edn/read-string)]
    (merge
      {"/" (get-home-page episodes)
       "/rss.xml" (rss/generate-rss episodes)}
      (zipmap (map #(str (:ep/link %) "/") episodes) (map get-episode-page episodes)))))

(defn get-assets []
  (assets/load-assets "public" [#".*"]))


(def app (-> (stasis/serve-pages get-pages)
             (optimus/wrap get-assets optimizations/none strategies/serve-live-assets)
             wrap-content-type
             prone/wrap-exceptions))
