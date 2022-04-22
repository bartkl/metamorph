(ns schema-transformer.spec.rdf
  (:require [clojure.spec.alpha :as s]))

(s/def :rdf/subject string?)
(s/def :rdf/predicate string?)
(s/def :rdf/object string?)
(s/def :rdf/context string?)

(s/def :rdf/triple (s/keys :req [:rdf/subject :rdf/predicate :rdf/object] :opt [:rdf/context]))
