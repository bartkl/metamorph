; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.graph.db
  (:require [asami.core :as d]))

(defn node-ref?
  "Checks if the provided hash-map is an Asami node reference."
  [m]

  (= (keys m) '(:id)))

(defn get-resource-iris
  "Gets the IRIs of all resources store in the database."
  [conn]

  (map first
       (d/q '[:find ?subj
              :where [?subj _ _]]
            conn)))

(defn add!
  "Adds the provided triples to the database, and then adds the
  necessary metadata for working with them, i.e. an entity mark and
  node ID. 

  This operation is blocking."
  [conn triples]

  (letfn [(transact-sync [conn triples]
            @(d/transact conn {:tx-triples triples}))
          (node-id [resource] [resource :id resource])
          (entity-mark [resource] [resource :a/entity true])]

    (transact-sync conn triples)
    (let [resources (get-resource-iris conn)
          metadata (mapcat #(list
                             (entity-mark %)
                             (node-id %))
                           resources)]
      (transact-sync conn metadata))))


(defn get-resource [conn iri]
  (d/entity conn iri true))

(defn entity-id [node]
  (get-in node [:id :id]))
