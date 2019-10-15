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
            [utviklingslandet-no.rss :as rss]
            [hiccup.core :as hiccup]))

(defn layout [{:keys [title og]} & body]
  (hiccup.page/html5 {:lang "no"}
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
     [:title (or title "Utviklingslandet")]
     (when-let [{:keys [title description]} og]
       (list
         [:meta {:property "og:locale" :content "no_NO"}]
         [:meta {:name "twitter:card" :content "summary"}]
         (when title
           (list
             [:meta {:property "og:title" :content title}]
             [:meta {:name "twitter:title" :content title}]))
         (when description
           (list
             [:meta {:property "og:description" :content description}]
             [:meta {:name "twitter:description" :content description}]))))
     [:link {:type "application/rss+xml" :rel "alternate" :title "utviklingslandet.no" :href "https://utviklingslandet.no/rss.xml"}]
     [:link {:rel "stylesheet" :href "/site.css"}]]
    [:body body]))

(defn get-home-page [episodes]
  (layout
    {:og {:title "Utviklingslandet - en podcast" :description "Finn Johnsen og August Lilleaas prøver så godt de kan å lære nye ting om programmering og sånt."}}

    [:h1 "Velkommen til Utviklingslandet!"]

    [:p "Nå skal vi kose oss."]

    [:p "Uviklingslandet er en podcast med "
     [:a {:href "http://johnsenf.blogspot.com/"} "Finn Johnsen"]
     " og "
     [:a {:href "https://augustl.com"} "August Lilleaas"]
     ". Vi er software-utviklere, og prøver så godt vi kan å lære oss nye ting om programmering og sånt."]


    [:img.boom-headshot {:src "/images/finn_da.jpg" :alt "Finn Johnsen"}]
    [:img.boom-headshot {:src "/images/august_da.jpg" :alt "August Lilleaas"}]

    [:p "Har du lyst til å høre på podcast?"]

    [:ul
     [:li [:a {:href "https://podcasts.apple.com/no/podcast/utviklingslandet/id1459807906"} "iTunes"]]
     [:li [:a {:href "https://pca.st/g86h"} "Pocket Casts"]]
     [:li [:a {:href "https://open.spotify.com/show/0KZhH21gfYnDzb3XL4Aw0z?si=PuF5PPAxQeWUBnlCTfhuMA"} "Spotify"]]
     [:li [:a {:href "https://podtail.com/no/podcast/utviklingslandet/"} "Podtail"]]
     [:li [:a {:href "https://www.youtube.com/channel/UChpu1nP54SaYlFznoecHqKg"} "youtube.com"] " (kanal med alle episodene)"]
     [:li [:a {:href "https://twitter.com/utviklingsland"} "twitter.com/utviklingsland"]]
     [:li [:a {:href "https://www.facebook.com/utviklingslandet/"} "facebook.com/utviklingslandet"]]
     [:li [:a {:href "https://www.instagram.com/utviklingslandet/"} "instagram.com/utviklingslandet"]]]

    [:p "Eller bare søk på " [:strong "Utviklingslandet"] " i en podcast-app nær deg."]

    [:h2 "Episoder"]
    (map
      (fn [episode]
        [:div.episode
         [:h3 [:a {:href (:ep/link episode)} (:ep/title episode)]]
         [:p (:ep/description episode)]])
      (reverse episodes))))

(defn get-episode-page [episode]
  (layout
    {:title (:ep/title episode)
     :og {:title (:ep/title episode) :description (:ep/description episode)}}

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
      :allowfullscreen "true"}]

    (:ep/shownotes episode)))

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
