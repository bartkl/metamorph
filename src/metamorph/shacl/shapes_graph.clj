; SPDX-FileCopyrightText: 2022 - 2023 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.shacl.shapes-graph
  "SHACL shapes graph.

### Example use:

```clojure
(def conn (load \"dev-resources/example_profile/Constraints.ttl\"))
(def b-shape (get conn
               :https:%2F%2Fw3id.org%2Fschematransform%2FExampleShape#BShape))
(node-shape? b-shape)  ;; => true
(property-shape? b-shape)  ;; => false
(def from-b-to-d-but-somehow-different-shape
  (property b-shape
    :https:%2F%2Fw3id.org%2Fschematransform%2FExampleVocabulary#FromBtoDButSomehowDifferent))
(pprint/pprint from-b-to-d-but-somehow-different-shape)
(pprint/pprint (first (:sh/and b-shape)))  ;; RDF lists are converted to Clojure vectors
```

"
  (:require
    [clojure.java.io :as io]
    [clojure.pprint :as pprint]
    [clojure.set :as set]
    [ont-app.vocabulary.core :as voc]
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
    [metamorph.utils.json :refer [encode-keyword]]
    [metamorph.vocabs.prof :as prof]
    [clojure.walk :as walk]
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

(defn- read-statements
  [path]
  (let [path (io/file path)]
    (cond
      (utils.file/file? path) (read-file path)
      (utils.file/dir? path) (read-dir path)
      :else :err)))

(defn- Statement->triple
  [st]
  (letfn [(IRI->kw [iri] (voc/keyword-for (str iri)))
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
                  (.isIRI %) (voc/keyword-for (str %))
                  :else (voc/keyword-for (str %)))
                st-obj)]
      [subj pred obj])))

;;;; asami  ;; todo: name this better
(defn store! [conn statements]
  @(d/transact conn {:tx-triples statements}))

(defn mark-entity [resource]
  [resource :a/entity true])

(defn add-id [resource]
  [resource :id resource])

;;;; shacl.query

(defn node-shape-iris
  [conn]
  (map first
    (d/q '[:find ?shape
           :where [?shape _ _]]
      conn)))

;;;; shacl.shapes-graph
(defn store-shapes!
  ([conn] (fn [triples] (println "Hello") (store-shapes! conn triples)))
  ([conn triples]
   (store! conn triples)
   (let [metadata (mapcat
                    #(list
                       (mark-entity %)
                       (add-id %))
                    (node-shape-iris conn))]
     (store! conn metadata))))

(defn load
  "Loads a shapes graph from files found at the provided path.

  If `path` is a single file, it will simply be read. If it's a directory,
  all files in it will be read.

  An Asami database is created with a fixed name, and it will be recreated
  on subsequent calls to `load`.

  Returns:
    Asami database URI.
"
  {:malli/schema [:=> [:cat :path] :asami/conn]}
  [path]
  (let [db-uri "asami:mem://shapes-graph"]
    (d/delete-database db-uri)
    (d/create-database db-uri)
    (let [conn (d/connect db-uri)]
      (->>
        (read-statements path)
        (map Statement->triple)
        (store-shapes! conn))
      conn)))

(defn- parse-rdf-lists
  [shape]
  (walk/prewalk
    #(if
       (and (:rdf/first %) (:rdf/rest %))
       (datatype/rdf-list->seq %)
       %)
    shape))

(defn get
  "Gets a shape.
"
  {:malli/schema [:=>
                  [:cat :asami/conn :sh/IRI]
                  [:or
                   :sh/NodeShape
                   :sh/PropertyShape]]}
  [conn iri]
  (->>
    (d/entity conn iri true)
    (parse-rdf-lists)))

(defn node-shapes
  "Get all node shapes.
"
  {:malli/schema [:=> [:cat] [:vector :sh/NodeShape]]}
  []
  :todo)

(defn property  ;; TODO: rename
  "Get the property shape that constrains the property identified by `sh:path`.
"
  [node-shape path]
  (->>
    (:sh/property node-shape)
    (filter #(= (:sh/path %) path))
    first))
      

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
  :todo
  true)

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
  (def conn (load "dev-resources/example_profile/Constraints.ttl"))
  (def b-shape (get conn :shape/BShape))
  (node-shape? b-shape)  ;; => true
  (property-shape? b-shape)  ;; => false
  (def from-b-to-d-but-somehow-different-shape
    (property b-shape
      :https:%2F%2Fw3id.org%2Fschematransform%2FExampleVocabulary#FromBtoDButSomehowDifferent))
  (pprint/pprint from-b-to-d-but-somehow-different-shape)
  (pprint/pprint (first (:sh/and b-shape)))  ;; RDF lists are converted to Clojure vectors

  "
  * Around the edges (public stuff and model): malli?
  * Include tests and documentation, especially around the edges
  * Use Malli parsing in entity fetching?
  * Think of error handling, invariants, assumptions and logging/UI messaging.

  * Is it possible to register ont-app namespaces for the shapes graph prefix?
    * Most challenging part is the potential reuse of shapes from other namespaces.
    Then again, these probably already have prefixes...

Use only SHACL, not vocab, not DX-PROF. Before feeding shapes graph into Metamorph, enrich it with descriptions and names using GraphDB or something:

CONSTRUCT {
  ?shape sh:description ?description ;
             sh:name ?className ;
}
WHERE {
  ?shape  
  ?label (
}

You still have targetClass and path parameters, so you still need to handle that vocabulary as well. How?

Better generic solution: fetch all namespaces with no prefix and create dynamic anonymous ones. Must be deterministic, so use sorting!

 TODO ^
"

  
  (defn namespaces [sts]
    (into {} (map
               #(vector (.getPrefix %) (.getName %))
               (.getNamespaces sts))))

  (def sts (read-statements "dev-resources/example_profile/Constraints.ttl"))
  (namespaces sts)

  (map voc/ns-to-namespace (vals (voc/prefix-to-ns)))

  (def unnamed-keys (set/difference
                      (into #{} (keys (namespaces sts)))
                      (into #{} (keys (voc/prefix-to-ns)))))

  (vals (namespaces sts))
  (map voc/ns-to-namespace (vals (voc/prefix-to-ns)))
  (def unnamed (set/difference
                 (into #{} (vals (namespaces sts)))
                 (into #{} (map voc/ns-to-namespace (vals (voc/prefix-to-ns))))))

  unnamed-keys
  unnamed
  
  (doseq [[uri n] (map-indexed #(vector %2 %1) (sort unnamed))]
    (println (str uri n))
    (voc/put-ns-meta!
      (symbol (str "ont-app.vocabulary.ns" n))
      {
       :dc/description "Shapes graph"
       :vann/preferredNamespaceUri uri
       :vann/preferredNamespacePrefix (str "ns" n)
       }
      ))
  (voc/prefix-to-ns)
  (->
    ((voc/prefix-to-ns) "ns0")
    voc/ns-to-namespace)

  (->
    ((voc/prefix-to-ns) "ns1")
    voc/ns-to-namespace)
    
  )