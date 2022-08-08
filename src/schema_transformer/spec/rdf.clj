; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns schema-transformer.spec.rdf
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.spec.test.alpha :as spec-test])
  (:import (java.net URI URISyntaxException)))

(defn file? [f] (.isFile f))

(defn iri? [kw]
  (and (keyword? kw)
       (some? (try (uri? (URI. (name kw)))
                   (catch URISyntaxException e)))))

(s/def :rdf/blank-node #(string/starts-with? (name %) "_:"))
(s/def :rdf/literal some?)

(s/def :rdf/List (s/or
                  :list (s/keys :req [:rdf/rest]
                                :opt [:rdf/first])
                  :nil :rdf/nil))

(s/def :rdf/nil #{:rdf/nil})
(s/def :rdf/rest :rdf/List)

(s/def :rdf/subject (s/or :iri iri? :blank-node ::blank-node))
(s/def :rdf/predicate iri?)
(s/def :rdf/object :rdf/literal)

(s/def :rdf/triple (s/tuple :rdf/subject :rdf/predicate :rdf/object))

(comment
  (def r {:rdf/first 1 :rdf/rest :rdf/nil})
  (s/explain :rdf/List :rdf/nil))