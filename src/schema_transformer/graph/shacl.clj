; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns schema-transformer.graph.shacl
  (:require [schema-transformer.rdf.datatype :as datatype]
            [schema-transformer.graph.db :as graph.db]
            [clojure.string :as string]))

(defn blank-node? [kw]
  (string/starts-with? (str kw) ":_:"))

(declare properties)

(defn- inherited-properties [node-shape]
  (let [other-shapes (datatype/rdf-list->seq (node-shape :sh/and))]
    (if (seq other-shapes)
      (mapcat properties other-shapes)
      '())))

(defn- own-properties [node-shape]
  (let [prop (:sh/property node-shape)]
    (->>
     (if (map? prop) (list prop) prop)
     (filter #(not (graph.db/node-ref? (:sh/node %)))))))

(defn properties [node-shape]
  (into #{} (concat
             (own-properties node-shape)
             (inherited-properties node-shape))))