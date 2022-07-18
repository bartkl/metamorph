; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns schema-transformer.graph.db
  (:require [asami.core :as d]))

(defn node-ref? [m]
  (= (keys m) '(:id)))

(defn get-resources [conn]
  (map first
       (d/q '[:find ?subj
              :where [?subj _ _]]
            conn)))

(defn store [statements conn])

(defn- mark-entity [n] (vector n :a/entity true))
(defn- add-id [n] (vector n :id n))

(defn mark-resources-as-entities [conn]
  @(d/transact conn {:tx-triples
                     (mapcat #(list
                               (mark-entity %)
                               (add-id %))
                             (get-resources conn))}))
