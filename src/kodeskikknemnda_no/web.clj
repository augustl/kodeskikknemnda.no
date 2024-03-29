(ns kodeskikknemnda-no.web
  (:require [stasis.core :as stasis]
            [optimus.assets :as assets]
            [optimus.prime :as optimus]
            [optimus.strategies :as strategies]
            [optimus.optimizations :as optimizations]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [hiccup.page]
            [prone.middleware :as prone]
            [clojure.edn]
            [kodeskikknemnda-no.rss :as rss]
            [hiccup.core :as hiccup]))

(defn layout [{:keys [title og]} & body]
  (hiccup.page/html5 {:lang "no"}
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
     [:title (or title "Kodeskikknemnda")]
     (when-let [{:keys [title description photo-url]} og]
       (list
         [:meta {:property "og:locale" :content "no_NO"}]
         [:meta {:name "twitter:card" :content "summary"}]
         [:meta {:name "twitter:creator:id" :content "kodeskikknemnda"}]
         (when title
           (list
             [:meta {:property "og:title" :content title}]
             [:meta {:name "twitter:title" :content title}]))
         (when description
           (list
             [:meta {:property "og:description" :content description}]
             [:meta {:name "twitter:description" :content description}]))
         (when photo-url
           (list
             [:meta {:property "og:image" :content photo-url}]
             [:meta {:name "twitter:image" :content photo-url}]))))
     [:link {:type "application/rss+xml" :rel "alternate" :title "kodeskikknemnda.no" :href "https://kodeskikknemnda.no/rss.xml"}]
     [:link {:rel "stylesheet" :href "/site.css"}]]
    [:body body]))

(defn get-home-page [episodes]
  (layout
    {:og {:title "Kodeskikknemnda - en podcast" :description "En podcast om alt som rører seg i kode-landet Norge"}}

    [:h1 "Velkommen til kodeskikknemnda!"]

    [:p "Denne websiden er UNDER CONSTRUCTION."]

    [:p "Har du lyst til å høre på podcast?"]

    [:ul
     [:li [:a {:href "https://podcasts.apple.com/no/podcast/kodeskikknemnda/id1516687555"} "iTunes"]]
     [:li [:a {:href "https://www.youtube.com/channel/UCsjaBvxO4C7nMyebtkR_TQQ"} "youtube.com"] " (kanal med alle episodene)"]
     [:li [:a {:href "https://open.spotify.com/show/0EniYuh0XXGEVS80lQ0MKR"} "Spotify"]]
     #_ [:li [:a {:href "https://pca.st/g86h"} "Pocket Casts"]]
     #_ [:li [:a {:href "https://podtail.com/no/podcast/kodeskikknemnda/"} "Podtail"]]]

    [:p "Eller bare søk på " [:strong "kodeskikknemnda"] " i en podcast-app nær deg."]

    [:p "Følg oss også gjerne på sosiale medier!"]

    [:ul
     [:li [:a {:href "https://twitter.com/kodeskikknemnda"} "Twitter (@kodeskikknemnda)"]]]

    [:h2 "Episoder"]
    (map
      (fn [episode]
        [:div.episode
         [:div.episode-photo
          [:img {:src (:ep/photo-url-thumb episode)}]]
         [:div.episode-info
          [:h3 [:a {:href (:ep/link episode)} (:ep/title episode)]]
          (let [[y m d] (:ep/pub-date episode)]
            [:p {:style "font-size: .9em"} (str y "-" m "-" d)])
          [:p (:ep/description episode)]]])
      episodes)))

(defn get-episode-page [episode]
  (layout
    {:title (:ep/title episode)
     :og {:title (:ep/title episode) :description (:ep/description episode) :photo-url (:ep/photo-url episode)}}

    [:p [:a {:href "/"} "< Tilbake til forsiden"]]
    [:h1 (:ep/title episode)]
    [:p [:strong (:ep/subtitle episode)]]
    [:p (:ep/description episode)]
    [:div.youtube-embed-container
     [:iframe
      {:src (str "https://www.youtube.com/embed/" (:ep/youtube-id episode))
       :frameborder "0"
       :allow "accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture"
       :allowfullscreen "true"}]]

    [:div.shownotes
     [:p {} [:img {:style "max-width: 560px" :src (:ep/photo-url episode)}]]
     (:ep/shownotes episode)]))

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
