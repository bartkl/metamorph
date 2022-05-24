(ns schema-transformer.schemas.avro.schema
  (:require [schema-transformer.schemas.avro.enum :refer [shape->enum]]
            [schema-transformer.schemas.avro.record :refer [shape->record]]))


(defn avro-schema [shape]
  (if (contains? shape :sh/in)
    (shape->enum shape)
    (shape->record shape)))
