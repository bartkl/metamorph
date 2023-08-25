; SPDX-FileCopyrightText: 2023 Bart Kleijngeld
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.graph.shacl
  (:require [metamorph.rdf.datatype :as datatype]
    [metamorph.graph.db :as graph.db]
    [clojure.string :as string]
    [asami.core :as d]
    [metamorph.utils.uri :as utils.uri]))

(defn blank-node? [kw]
  (string/starts-with? (name kw) "_:"))

(declare properties)

(defn get-node-shapes [conn]
  (flatten
    (d/q '[:find ?nodeShape
           :where [?nodeShape :rdf/type :sh/NodeShape]]
      conn)))

(defn- inherited-properties [node-shape]
  (let [other-shapes (datatype/rdf-list->seq (node-shape :sh/and))]
    (if (seq other-shapes)
      (mapcat properties other-shapes)
      '())))

(defn- own-properties [node-shape]
  (let [prop (:sh/property node-shape)]
    (if (map? prop) (list prop) prop)))

(defn property-node-ref? [property-shape]
  (graph.db/node-ref? (:sh/node property-shape)))

(defn properties [node-shape]
  (into #{} (concat
              (own-properties node-shape)
              (inherited-properties node-shape))))
