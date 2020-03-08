(ns food.fetch
  (:require [clj-http.client :as client]
            [clojure-csv.core :as csv]
            [food.secret :as secret]
            [semantic-csv.core :as sc]
            [cheshire.core :refer [generate-string parse-string]])
  (:gen-class))

(defn query-yelp [m]
  (try
   (client/get
     "https://api.yelp.com/v3/businesses/search"
     {:headers {"Authorization" secret/token}
      :query-params
       {"term" (:COMPANY m)
        "location" (:ADDRESS m)}})
   (catch Exception e {:exception e})))

(defn parse-response [m]
  (update-in m [:yelp :body] parse-string))

(defn remove-substr [regex]
  (fn [s] (clojure.string/replace s regex "")))

(def strip-trailing-the
  (remove-substr #", THE$"))

(def strip-leading-the
  (remove-substr #"^THE "))

(def strip-store-qualifiers
  (remove-substr #"\s*[@#].*"))

(def strip-parens
  (remove-substr #"\s*\(.*"))

(let [scores
      {"A+" 0
       "A"  1
       "B+" 2
       "B"  3
       "C+" 4
       "C"  5
       "F"  6}]
  (defn sort-by-rating [coll]
    (sort-by #(scores (:LATEST_SCORE %)) > coll)))

(defn normalize-company [m]
  (update m :COMPANY (comp clojure.string/lower-case
                           clojure.string/trim
                           strip-parens
                           strip-store-qualifiers
                           strip-leading-the
                           strip-trailing-the)))

(def irving-food-establishment-scores
  "http://drive.google.com/uc?export=download&id=1e3W9gAG4z2g2qkgmROwOS3OtjkMThWDM")

(def utf8-bom "\uFEFF")

(defn strip-byte-order-mark [s]
  (if (clojure.string/starts-with? s utf8-bom)
    (.substring s 1)
    s))

(defn -main [& args]
  (->> (client/get irving-food-establishment-scores)
       :body
       strip-byte-order-mark
       csv/parse-csv
       sc/remove-comments
       sc/mappify
       (map #(assoc % :yelp (query-yelp %)))
       (map parse-response)
       (remove #(= "" (:LATEST_SCORE %)))
       (map #(assoc % :latitude
               (get-in % [:yelp :body "businesses" 0 "coordinates" "latitude"])))
       (map #(assoc % :longitude
               (get-in % [:yelp :body "businesses" 0 "coordinates" "longitude"])))
       (map #(dissoc % :yelp))
       (map normalize-company)
       (group-by :COMPANY)
       (reduce-kv #(assoc %1 %2 (sort-by-rating %3)) {})
       (#(generate-string % {:pretty true}))
       (spit "foodhealth.json")))
