; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.graph.db-test
  (:require [clojure.test :refer :all]
            [asami.core :as d]
            [metamorph.graph.db :refer :all]))

(def db-uri "asami:mem://test-db")

(def ^:dynamic *conn* nil)

(defn setup-conn [f]
  (d/create-database db-uri)
  (binding [*conn* (d/connect db-uri)]
    (f)
    (d/delete-database db-uri)))

(use-fixtures :each setup-conn)

;; Tests
(deftest test-node-ref?
  (testing "Returns node-ref for existing, proper node in Asami"
    @(d/transact
      *conn* {:tx-data
              [{:db/ident "home-boys"
                :bart {:db/ident 1 :name "Bart" :colleague {:db/ident 2}}
                :rick {:db/ident 2 :name "Rick" :colleague {:db/ident 1}}}]}))
  (let [actual (node-ref? (get-in (d/entity *conn* 1) [:colleague :colleague]))]
    (is (= true actual))))

(deftest test-store-resources!)

(deftest test-resource)
