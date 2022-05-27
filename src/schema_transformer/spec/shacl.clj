(ns schema-transformer.spec.shacl
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]))

(s/def :sh/targetClass ::iri)
(s/def :sh/and (s/* ::iri))
(s/def :rdfs/comment string?)
(s/def :rdfs/label string?)
(s/def :sh/property (s/* :sh/propertyShape))

(s/def :sh/path ::iri)
(s/def :sh/datatype #{:xsd/string :xsd/double :xsd/boolean
                      :xsd/int :xsd/decimal :xsd/float :xsd/duration
                      :xsd/dateTime :xsd/date :xsd/time :xsd/anyURI})
(s/def :sh/node ::iri)
(s/def :sh/minCount int?)
(s/def :sh/maxCount int?)
(s/def :sh/in (s/cat))

(s/def :sh/propertyShape
  (s/keys :req [:sh/path]
          :opt [:sh/node :sh/minCount :sh/maxCount]))
          ;; TODO: How can I say: one of (and not both) `:sh/node` or `sh:datatype` is required?

(s/def :sh/nodeShape
  (s/keys :req [:sh/targetClass]
          :opt [:sh/property :rdfs/comment :rdfs/label :sh/in]))