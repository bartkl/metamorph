; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns schema-transformer.graph.db
  (:require [asami.core :as d]
            [ont-app.vocabulary.core :as vocab]))

(defn node-ref? [m]
  (= (keys m) '(:id)))

(defn- resource-iris [conn]
  (map first
       (d/q '[:find ?subj
              :where [?subj _ _]]
            conn)))

(defn store! [conn statements]
  @(d/transact conn {:tx-triples statements}))

(defn store-resources! [conn statements]
  (store! conn statements)
  (let [resources (resource-iris conn)
        entity-for (fn [resource] [resource :a/entity true])
        id-for (fn [resource] [resource :id resource])
        metadata (mapcat #(list
                           (entity-for %)
                           (id-for %))
                         resources)]
    (store! conn metadata)))

(defn resource [conn iri]
  (d/entity conn (vocab/keyword-for iri) true))