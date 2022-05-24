(ns schema-transformer.utils.uri
  (:import (java.net URI)))

(defn iri-local-name [kw]
  (as-> (str kw) v
    (subs v 1)
    (URI. v)
    (.getFragment v)
    (keyword v)))