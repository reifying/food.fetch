(ns food.fetch-test
  (:require [clojure.test :refer :all]
            [food.fetch :refer :all]))

(deftest test-strip-trailing-the
  (testing "Should remove trailing THE"
    (is (= "GOAT" (strip-trailing-the "GOAT, THE")))))

(deftest test-strip-leading-the
  (testing "Should remove leading THE"
    (is (= "GOAT" (strip-leading-the "THE GOAT")))))

(deftest test-strip-store-qualifiers
  (testing "Should remove restaurant qualifiers"
    (is (= "EUREST DINING"
           (strip-store-qualifiers "EUREST DINING @ ADT-IRVING")))
    (is (= "EUREST DINING"
           (strip-store-qualifiers "EUREST DINING #122")))))

(deftest test-strip-parens
  (testing "Should remove parenthetical qualifiers"
    (is (= "EUREST DINING"
           (strip-parens
             "EUREST DINING (ADT-IRVING) unlikely trailing text")))))

(deftest test-sort-by-rating
  (testing "Should list worst ratings first"
    (is (= [{:LATEST_SCORE "C"}
            {:LATEST_SCORE "C+"}]
           (sort-by-rating
             [{:LATEST_SCORE "C+"}
              {:LATEST_SCORE "C"}])))))

(deftest test-normalize-company
  (testing "Should clean up company to more-likely spoken name"
    (is (= {:COMPANY "taco bell"}
           (normalize-company {:COMPANY " Taco Bell #322"})))))
