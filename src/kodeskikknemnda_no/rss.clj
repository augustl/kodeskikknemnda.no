(ns kodeskikknemnda-no.rss
  (:require [hiccup.core :as hiccup]
            [hiccup.page])
  (:import (java.time ZonedDateTime LocalDateTime ZoneId)
           (java.time.format DateTimeFormatter)))

(defn parse-ep-pub-date [[y m d & [h min]]]
  (ZonedDateTime/of (LocalDateTime/of y m d (or h 0) (or min 0)) (ZoneId/of "Europe/Oslo")))

(defn get-pub-date [episodes]
  (->> episodes
       (map #(parse-ep-pub-date (:ep/pub-date %)))
       (sort)
       (reverse)
       (first)))

(defn cdata [val]
  (str "<![CDATA[\n" val "\n]]>"))

(defn generate-rss [episodes]
  (hiccup/html
    {:mode :xml}
    (hiccup.page/xml-declaration "UTF-8")
    [:rss {"xmlns:itunes" "http://www.itunes.com/dtds/podcast-1.0.dtd" :version "2.0"
           "xmlns:content" "http://purl.org/rss/1.0/modules/content/"}
     [:channel
      [:title "Kodeskikknemnda"]
      [:description "En podcast om alt som rører seg i kode-landet Norge"]
      [:link "https://kodeskikknemnda.no"]
      [:language "no"]
      [:copyright "Copyright 2019"]
      [:docs "http://blogs.law.harvard.edu/tech/rss"]
      [:webMaster "august@augustl.com"]
      [:itunes:author "Kodeskikknemnda"]
      [:itunes:subtitle "August Lilleaas prater med folk om software"]
      [:itunes:summary "En podcast om alt som rører seg i kode-landet Norge"]
      [:itunes:owner
       [:itunes:name "kodeskikknemnda.no"]
       [:itunes:email "august@augustl.com"]]
      [:itunes:explicit "No"]
      [:itunes:type "episodic"]
      [:itunes:image {:href "https://cdn.kodeskikknemnda.no/cover-art.jpg"}]
      [:itunes:category {:text "Technology"}
       [:itunes:category {:text "Programming"}]]
      [:pubDate (.format DateTimeFormatter/RFC_1123_DATE_TIME (get-pub-date episodes))]
      [:lastBuildDate (.format DateTimeFormatter/RFC_1123_DATE_TIME (ZonedDateTime/now))]
      (map
        (fn [ep]
          [:item
           [:title (:ep/title ep)]
           [:link (str "https://kodeskikknemnda.no" (:ep/link ep))]
           [:guid (:ep/guid ep)]
           [:description (:ep/description ep)]
           [:content:encoded
            (cdata (hiccup/html (concat (list [:p (:ep/description ep)]) (:ep/shownotes ep))))]
           [:enclosure {:url (:ep/file-url ep) :type "audio/mpeg"}]
           [:category "Software development"]
           (let [ep-pub-date (:ep/pub-date ep)]
             [:pubDate (.format DateTimeFormatter/RFC_1123_DATE_TIME (parse-ep-pub-date ep-pub-date))])
           [:itunes:author "Kodeskikknemnda"]
           [:itunes:explicit (if (:ep/explicit ep) "Yes" "No")]
           (when-let [subtitle (:ep/subtitle ep)]
             [:itunes:subtitle subtitle])
           [:itunes:summary]
           [:itunes:duration (:ep/duration ep)]
           [:itunes:keywords (clojure.string/join ", " (:ep/keywords ep))]])
        episodes)]]))
