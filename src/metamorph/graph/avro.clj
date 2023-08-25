; SPDX-FileCopyrightText: 2023 Bart Kleijngeld
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.graph.avro
  (:require [deercreeklabs.lancaster :as l]
    [asami.core :as d]
    [metamorph.graph.db :as graph.db]
    [metamorph.graph.shacl :as shacl]
    [metamorph.rdf.datatype :refer [rdf-list->seq]]
    [metamorph.schemas.avro.cardinality :refer [cardinality->schema-fn]]
    [metamorph.schemas.avro.datatype :refer [xsd->avro]]
    [metamorph.utils.uri :as utils.uri]))

(defn get-root-node-shape-uri [conn]
  (first (first
          (d/q '[:find ?nodeShape
                 :where [?nodeShape :rdf/type :sh/NodeShape]
                 [?nodeShape :rdfs/comment "RootObject"]]
            conn))))
