; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.graph.db
  (:require [asami.core :as d]))

(defn node-ref?
  "Checks if the provided hash-map is an Asami node reference."
  [m]
  (= (keys m) '(:id)))

(defn- resource-iris [conn]
  (map first
       (d/q '[:find ?subj
              :where [?subj _ _]]
            conn)))

(defn store! [conn statements]
  @(d/transact conn {:tx-triples statements}))

(defn mark-entity [resource]
  [resource :a/entity true])

(defn add-id [resource]
  [resource :id resource])

(defn store-resources! [conn statements]
  (store! conn statements)
  (let [resources (resource-iris conn)
        metadata (mapcat #(list
                           (mark-entity %)
                           (add-id %))
                         resources)]
    (store! conn metadata)))

(defn resource [conn iri]
  (d/entity conn iri true))
