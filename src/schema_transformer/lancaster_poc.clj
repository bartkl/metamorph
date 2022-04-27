(ns schema-transformer.lancaster-poc
  (:require [deercreeklabs.lancaster :as l]
            [clojure.java.io :as io]))

(l/def-record-schema D
  "This is yet another class"
  [:id :required l/string-schema]
  [:def l/int-schema])

(l/def-record-schema B
  "This is a sub-class"
  [:fromBtoD "Association from B to D" :required D]
  [:fromBtoDButSomehowDifferent "Association from B to D but somehow different" :required (l/map-schema D)])


(with-open [w (io/writer "testme.json")]
  (.write w (l/json B)))

(l/default-data B)
(l/edn B)



;; Primitive type names are also defined type names. Thus, for example, the schema "string" is equivalent to:

;; {"type": "string"}