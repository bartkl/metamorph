(ns schema-transformer.utils.uri
  (:require [ont-app.vocabulary.core :as vocab]) 
  (:import (java.net URI)))

(defn iri-local-name [kw]
  (->> (vocab/uri-for kw)
       URI.
       .getFragment
       keyword))