(ns utviklingslandet-no.rss
  (:require [hiccup.core :as hiccup]
            [hiccup.page])
  (:import (java.time ZonedDateTime LocalDateTime ZoneId)
           (java.time.format DateTimeFormatter)))

(defn parse-ep-pub-date [[y m d]]
  (ZonedDateTime/of (LocalDateTime/of y m d 0 0) (ZoneId/of "Europe/Oslo")))

(defn get-pub-date [rss-data]
  (->> (:episodes rss-data)
       (map #(parse-ep-pub-date (:ep/pub-date %)))
       (sort)
       (reverse)
       (first)))

(defn generate-rss [rss-data]
  (hiccup/html
    {:mode :xml}
    (hiccup.page/xml-declaration "UTF-8")
    [:rss {"xmlns:itunes" "http://www.itunes.com/dtds/podcast-1.0.dtd" :version "2.0"}
     [:channel
      [:title "Utviklingslandet"]
      [:description "Vi snakker om programmering"]
      [:link "https://utviklingslandet.no"]
      [:language "no"]
      [:copyright "Copyright 2019"]
      [:docs "http://blogs.law.harvard.edu/tech/rss"]
      [:webMaster "mail@utviklingslandet.no"]
      [:itunes:author "utviklingslandet.no"]
      [:itunes:subtitle "Vi snakker om programmering"]
      [:itunes:summary "Finn Johnsen og August Lilleaas prøver så godt de kan å lære nye ting om programmering og sånt."]
      [:itunes:owner
       [:itunes:name "utviklingslandet.no"]
       [:itunes:email "mail@utviklingslandet.no"]]
      [:itunes:explicit "Yes"]
      [:itunes:image {:href "https://f002.backblazeb2.com/file/utviklingslandet-public/logo.jpg"}]
      [:itunes:category {:text "Technology"}
       [:itunes:category {:text "Programming"}]]
      [:pubDate (.format DateTimeFormatter/RFC_1123_DATE_TIME (get-pub-date rss-data))]
      [:lastBuildDate (.format DateTimeFormatter/RFC_1123_DATE_TIME (ZonedDateTime/now))]
      (map
        (fn [ep]
          [:item
           [:title (:ep/title ep)]
           [:link (str "https://utviklingslandet.no" (:ep/link ep))]
           [:guid (:ep/guid ep)]
           [:description (:ep/description ep)]
           [:enclosure {:url (:ep/file-url ep) :type "audio/mpeg"}]
           [:category "Software development"]
           (let [ep-pub-date (:ep/pub-date ep)]
             [:pubDate (.format DateTimeFormatter/RFC_1123_DATE_TIME (parse-ep-pub-date ep-pub-date))])
           [:itunes:author "Utviklingslandet"]
           [:itunes:explicit (if (:ep/explicit ep) "Yes" "No")]
           (when-let [subtitle (:ep/subtitle ep)]
             [:itunes:subtitle subtitle])
           [:itunes:summary]
           [:itunes:duration (:ep/duration ep)]
           [:itunes:keywords (clojure.string/join ", " (:ep/keywords ep))]])
        (:episodes rss-data))]]))
