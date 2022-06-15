(ns schema-transformer.schemas.sql.schema
  (:require [honey.sql :as sql]
            [honey.sql.helpers :as h]
            [schema-transformer.graph.shacl :as graph.shacl]
            [schema-transformer.graph.db :as graph.db]
            [schema-transformer.utils.uri :as utils.uri]))

(def xsd->sql
  #:xsd{:string [:varchar 255]
        :boolean :boolean
        :decimal :decimal
        :float [:float 63 6]
        :int [:int 31]
        :double [:double 63 6]
        :duration [:double 63 6]
        :dateTime :datetime
        :date :datetime
        :time :datetime
        :anyURI [:varchar 255]})

(defn id-column [] [:id [:varchar 255] [:not nil] [:primary-key]])

(defn foreign-key-column [property-shape])

(defn primitive-column [property-shape])

(declare table)

(defn class-name [class]
  (utils.uri/iri-local-name (get-in class [:id :id])))

(defn column [property-shape]
  (let [name (class-name (:sh/path property-shape))
        datatype (property-shape :sh/datatype)
        node (property-shape :sh/node)
        type (cond
               (some? datatype) (xsd->sql datatype)
               (not (graph.db/node-ref? node)) (class-name node))]

    [name type]))

(defn table [node-shape]
    (h/create-table
     (class-name (node-shape :sh/targetClass))
     (h/with-columns (cons
                      (id-column)
                      (map column (graph.shacl/properties node-shape))))))