; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns schema-transformer.graph.db-test
  (:require [clojure.test :refer :all]
            [asami.core :as d]
            [schema-transformer.graph.db :refer :all]))

(def db-uri "asami:mem://test-db")
(def ^:dynamic *conn* nil)

(defn setup-conn [f]
  (d/create-database db-uri)
  (d/connect db-uri)
  (binding [*conn* (d/connect db-uri)]
    (f)
    (d/delete-database db-uri)))

(use-fixtures :each setup-conn)

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
