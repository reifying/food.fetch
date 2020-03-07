(ns food.fetch
  (:require [clj-http.client :as client]
            [clojure-csv.core :as csv]
            [food.secret :as secret]
            [semantic-csv.core :as sc]
            [cheshire.core :refer :all]
            [clojure.data :refer [diff]]
            [clojure.java.io :as io])
  (:gen-class))

; TODO pass as arg to main
(def csv-path "/Users/travis/Downloads/Food_Establishment_Scores.csv")

(def data
  (with-open [in-file (io/reader csv-path)]
       (->>
         (csv/parse-csv in-file)
         (sc/remove-comments)
         (sc/mappify)
         doall)))

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

(def data-with-yelp
  (doall (map #(assoc % :yelp (query-yelp %)) data)))

(def cleaned
  (map #(update-in % [:yelp :http-client] (fn [_] nil))
      data-with-yelp))

(spit "data-with-yelp.edn"
      (pr-str cleaned))

(def scores
  {"A+" 0
   "A"  1
   "B+" 2
   "B"  3
   "C+" 4
   "C"  5
   "F"  6})

(defn strip-trailing-the [m]
  (update m :COMPANY
    clojure.string/replace #", THE$" ""))

(defn strip-leading-the [m]
  (update m :COMPANY
    clojure.string/replace #"^THE " ""))

(defn strip-store-qualifiers [m]
  (update m :COMPANY
    clojure.string/replace #"\s*[@#].*" ""))

(defn strip-parens [m]
  (update m :COMPANY
      clojure.string/replace #"\(.*" ""))

(defn strip-spaces [m]
  (update m :COMPANY
    clojure.string/trim))

(defn lower-case-company [m]
  (update m :COMPANY clojure.string/lower-case))

(defn sort-by-rating [coll]
  (sort-by #(scores (:LATEST_SCORE %)) > coll))

(->> (clojure.edn/read-string (slurp "data-with-yelp.edn"))
     (map parse-response)
     (remove #(= "" (:LATEST_SCORE %)))
     (map #(assoc % :latitude (get-in % [:yelp :body "businesses" 0 "coordinates" "latitude"])))
     (map #(assoc % :longitude (get-in % [:yelp :body "businesses" 0 "coordinates" "longitude"])))
     (map #(dissoc % :yelp))
     (map strip-trailing-the)
     (map strip-leading-the)
     (map strip-store-qualifiers)
     (map strip-spaces)
     (map strip-parens)
     (map lower-case-company)
     (group-by :COMPANY)
     (reduce-kv #(assoc %1 %2 (sort-by-rating %3)) {})
     (reduce-kv #(assoc %1 (clojure.string/lower-case %2) %3) {})
     (#(generate-string % {:pretty true}))
     (spit "foodhealth.json"))

; (distinct (map :LATEST_SCORE (remove #(= "" (:LATEST_SCORE %)) d2)))
;
;
; (map (fn [m] {:orig-addr (:ADDRESS m)
;               :match-addr (get-in m [:yelp :body "businesses" 0 "location" "address1"])})
;   (filter #(< 1 (get-in % [:yelp :body "total"]))
;           (map parse-response data-with-yelp)))
;
; (map #(get-in % [:yelp :body "total"])
;      (map parse-response data-with-yelp))
;
; (:yelp (first (map parse-response data-with-yelp)))
;
; (count (filter #(= 200 (:status %)) (map :yelp data-with-yelp)))
