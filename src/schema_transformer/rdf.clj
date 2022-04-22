(ns schema-transformer.rdf
  (:require [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [schema-transformer.utils :as utils])
  (:import (org.eclipse.rdf4j.rio Rio)
           (org.eclipse.rdf4j.rio RDFFormat)))


(defn- simple-statement->map
  "Creates a triple-map from an rdf4j `SimpleStatement.`"

  [st]
  (let [ctx (.getContext st)]
    (as-> #:rdf{:subject (.getSubject st)
                :predicate (.getPredicate st)
                :object (.getObject st)} v
      (conj v (when ctx [:rdf/contexts ctx]))
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

  nil
  ;; TODO.
)

;; (read-directory (io/file "resources/example-profile/") (fn [f] (str "http://" (.getName f))))
(read-file (io/file "resources/example-profile/Profile.ttl") utils/iri-from-file-name)