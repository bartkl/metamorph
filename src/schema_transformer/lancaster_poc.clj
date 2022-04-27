(ns schema-transformer.lancaster-poc
  (:require [deercreeklabs.lancaster :as l]
            [clojure.java.io :as io]))

(l/def-record-schema D
  "This is yet another class"
  [:id l/string-schema])
  ;; [:def (l/union-schema [l/null-schema l/string-schema])])

(l/def-record-schema B
  "This is a subclass"
  [:FromBtoD D])

(with-open [w (io/writer "testme.json")]
  (.write w (l/json person-schema)))

(l/edn person-schema)

(l/def-record-schema person-schema
  [:name l/string-schema]
  [:age l/int-schema])