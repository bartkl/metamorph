(ns schema-transformer.rdf
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.spec.alpha :as s]
            [schema-transformer.utils :as utils])
  (:import (org.eclipse.rdf4j.rio Rio)
           (org.eclipse.rdf4j.model IRI)
           (org.eclipse.rdf4j.rio RDFFormat)
           (org.eclipse.rdf4j.model.util Values)))


(def supported-files #{"ttl", "rdf", "jsonld"})

(defn- simple-statement->map
  "Creates a triple-map from an rdf4j `SimpleStatement.`"

  [st]
  (let [ctx (.getContext st)]
    (as-> #:rdf{:subject (.getSubject st)
                :predicate (.getPredicate st)
                :object (.getObject st)} v
      (conj v (when ctx [:rdf/context (str ctx)]))
      (utils/map-values str v))))

(defn read-file
  "Reads RDF file."

  [path ctx-fn]
  {:pre [(.isFile path) (fn? ctx-fn)]
   :post [(s/valid? (s/coll-of :rdf/triple) %)]}

  ;; TODO: Make context function optional. Using `let`?
  (with-open [rdr (clojure.java.io/reader path)]
    (into (hash-set)
          (map simple-statement->map
               (Rio/parse rdr RDFFormat/TURTLE (ctx-fn path))))))

(defn read-directory
  "Reads all RDF files found in `path` and returns a set of all
   statements. The `ctx-fn` is used to generate a context dependent
   on the filename."

  [path ctx-fn]
  {:pre [(.isDirectory path) (fn? ctx-fn)]
   :post [(s/valid? (s/coll-of :rdf/triple) %)]}

  (->> (file-seq path)
       (filter #(supported-files (utils/file-ext %)))
       (map #(read-file % ctx-fn))
       (reduce set/union)))

(read-directory (io/file "resources/example-profile/")
                (fn [path] (into-array IRI [(utils/iri-from-filename path)])))

;; TODO: Improve this call... See below for a start, although that too seems sub-optimal.
(read-file (io/file "resources/example-profile/Profile.ttl")
           (fn [path] (into-array IRI [(utils/iri-from-filename path)
                                       (Values/iri "http://some-other-iri")])))

;; (utils/apply-fns-to-arg [#(utils/iri-from-filename %)
;;                          #(Values/iri "http://some-other-iri")]
;;                         (io/file "resources/example-profile/Profile.ttl"))

;; (utils/apply-fns-to-arg [+ *] 1 2)