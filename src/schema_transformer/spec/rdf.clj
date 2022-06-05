(ns schema-transformer.spec.rdf
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string])
  (:import (java.net URI URISyntaxException)))

(defn file? [f] (.isFile f))

(defn iri? [kw]
  (and (keyword? kw)
       (some? (try (uri? (URI. (name kw)))
                   (catch URISyntaxException e)))))

;; (s/fdef schema-transformer.rdf.reading/read-file
;;   :args [file?]  ;; TODO: Must be sequence?
;;   :ret string?)

(s/def :rdf/blank-node #(string/starts-with? (name %) "_:"))
(s/def :rdf/literal some?)

;; TODO: Fix RDF list spec.
(s/def ::aa (s/keys :req [:rdf/first :rdf/rest]))

(s/def :rdf/nil #{:rdf/nil})
(s/def :rdf/rest (s/or :rdf/nil :rdf/List))
(s/def :rdf/List
  (s/or :nil :rdf/nil
        :map ::aa))
;;

(s/def :rdf/subject (s/or :iri iri? :blank-node ::blank-node))
(s/def :rdf/predicate iri?)
(s/def :rdf/object :rdf/literal)

(s/def :rdf/statement (s/tuple :rdf/subject :rdf/predicate :rdf/object))
