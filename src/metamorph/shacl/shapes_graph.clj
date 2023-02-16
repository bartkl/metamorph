; SPDX-FileCopyrightText: 2022 - 2023 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.shacl.shapes-graph
  "SHACL shapes graph.

Example use:

```clojure
(def conn (load \"shapes.ttl\"))
(def b-shape (node-shape :ex/b-shape conn))
(:sh/targetClass b-shape)  ;; => :vocab/B
(node-shape? :ex/b-shape)  ;; => true
(node-shape? b-shape)  ;; => true
```


"
  (:require
    [clojure.java.io :as io]
    [clojure.set :as set]
    [ont-app.vocabulary.core :as vocab]
    [metamorph.rdf.datatype :as datatype]
    [metamorph.utils.file :as utils.file]
    [expound.alpha :as expound]
    [metamorph.utils.cli :refer [kw->opt]]
    [deercreeklabs.lancaster :as l]
    [asami.core :as d]
    [clojure.java.io :as io]
    [malli.experimental :as mx]
    [clojure.string :as string]
    [cheshire.generate :refer [add-encoder]]
    [cheshire.core :as json]
    [metamorph.rdf.reading :as rdf]
    [metamorph.schemas.avro.schema :refer [avro-schema]]
    [metamorph.graph.avro :as graph.avro]
    [metamorph.graph.db :as graph.db]
    [ont-app.vocabulary.core :as vocab]
    [metamorph.utils.json :refer [encode-keyword]]
    [metamorph.vocabs.prof :as prof]
    [metamorph.vocabs.role :as role])
  (:import
    (org.eclipse.rdf4j.rio Rio)
    (org.eclipse.rdf4j.model IRI)
    (org.eclipse.rdf4j.rio RDFFormat))
  (:refer-clojure :exclude [get load])
  (:gen-class))

;;;; shacl.io
(defn read-file
  [path]
  (with-open [rdr (io/reader path)]
    (let [ctxs (into-array IRI [])]
      (Rio/parse rdr RDFFormat/TURTLE ctxs))))

(defn read-dir
  [path]
  (letfn [(file-supported? [path]
            (contains? #{"ttl"} (utils.file/ext path)))]
    (->>
      (file-seq path)
      (filter file-supported?)
      (map read-file)
      (reduce set/union))
    ))

(defn- read-statements!
  [path]
  (let [path (io/file path)]
    (cond
      (utils.file/file? path) (read-file path)
      (utils.file/dir? path) (read-dir path)
      :else :err)))

(defn- Statement->triple
  [st]
  (letfn [(IRI->kw [iri] (vocab/keyword-for (str iri)))
          (BNode->kw [bnode] (keyword (str bnode)))]
    (let [st-subj (.getSubject st)
          st-pred (.getPredicate st)
          st-obj (.getObject st)
          subj (#(if (.isIRI %)
                   (IRI->kw %)
                   (BNode->kw %))
                 st-subj)
          pred (IRI->kw st-pred)
          obj (#(cond
                  (.isLiteral %) (datatype/literal->type %)
                  (.isIRI %) (vocab/keyword-for (str %))
                  :else (vocab/keyword-for (str %)))
                st-obj)]
      [subj pred obj])))

;;;; shacl.shapes-graph

(defn load
  "Loads a shapes graph from files found at the provided path.

  If `path` is a single file, it will simply be read. If it's a directory,
  all files in it will be read.
"
  {:malli/schema [:=> [:cat :path] :asami-db/conn]}
  [path]
  (->>
    (read-statements! path)
    (map Statement->triple)
    ; store!))
    ))

(defn get
  "Gets a shape.
"
  {:malli/schema [:=> [:cat :sh/IRI] :sh/Shape]}
  [iri]
  :todo)

(defn shape
  "Gets a shape.
"
  {:malli/schema [:=> [:cat :sh/IRI] :sh/Shape]}
  [iri]
  :todo)

(defn node-shape
  "Gets a node shape.
"
  {:malli/schema [:=> [:cat :sh/IRI] :sh/NodeShape]}
  [iri]
  :todo)

(defn property-shape
  "Gets a property shape.
"
  {:malli/schema [:=> [:cat :sh/IRI] :sh/PropertyShape]}
  [iri]
  :todo)

(defn node-shapes
  "Get all node shapes.
"
  {:malli/schema [:=> [:cat] [:vector :sh/NodeShape]]}
  []
  :todo)

(defn property-shapes
  "Get all property shapes.
"
  {:malli/schema [:=> [:cat] [:vector :sh/PropertyShape]]}
  []
  :todo)

(defn blank-node?
  "Checks if `shape` is a blank node.
"
  {:malli/schema [:=> [:cat :sh/ShapeNode] :boolean]}
  [shape]
  :todo)

(defn shape?
  "Checks if resource is a shape.
"
  {:malli/schema [:=> [:cat :sh/Shape] :boolean]}
  [shape]
  :todo)

(defn property-shape?
  "Checks if resource is a property shape.
"
  {:malli/schema [:=> [:cat :sh/Shape] :boolean]}
  [shape]
  (and
    (shape? shape)
    (contains? shape :sh/path)))

(defn node-shape?
  "Checks if resource is a node shape.
"
  {:malli/schema [:=> [:cat :sh/Shape] :boolean]}
  [shape]
  (and
    (shape? shape)
    (not (property-shape? shape))))

(comment
  (def shapes-graph-file "dev-resources/example_profile/Constraints.ttl")
  (def conn (load shapes-graph-file))
  (println conn)

  

  "1. Read the turtle files into RDF4j model
   2. Load them into Asami
   3. Mark entities
   4. Provide API for interacting with SHACL
    - When fetching entities:
      - rdf-lists -> clj vec
      - convert XSD primitives

      Around the edges (public stuff and model): malli?

      Include tests and documentation.
"

  
  
  )