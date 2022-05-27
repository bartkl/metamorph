(ns schema-transformer.spec.rdf
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]))

(defn file? [f] (.isFile f))
(defn has-namespace? [kw] (some? (namespace kw)))

;; (s/fdef schema-transformer.rdf.reading/read-file
;;   :args [file?]  ;; TODO: Must be sequence?
;;   :ret string?)

(s/def ::iri (s/and keyword? has-namespace?))
(s/def ::blank-node (s/and keyword? #(string/starts-with? (name %) "_:")))
(s/def ::literal some?)

(s/def ::subject (s/or :iri ::iri :blank-node ::blank-node))
(s/def ::predicate ::iri)
(s/def ::object ::literal)

(s/def ::statement (s/tuple ::subject ::predicate ::object))

;; (s/explain ::statement [:a :b :c])
