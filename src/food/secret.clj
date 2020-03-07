(ns food.secret)

(def token (str "Bearer " (System/getenv "YELP_API_KEY")))
