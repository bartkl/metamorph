(ns schema-transformer.schemas.sql.schema
  (:require [honey.sql :as sql]
            [honey.sql.helpers :as h]
            [schema-transformer.graph.shacl :as graph.shacl]
            [schema-transformer.utils.uri :as utils.uri]))

(def xsd->sql
  #:xsd{:string :varchar
        :boolean :boolean
        :decimal :decimal
        :float :float
        :double :double})

(defn id-column [] [:id :varchar 255 [:not nil] [:primary-key]])

(defn foreign-key-column [property-shape])

(defn primitive-column [property-shape])

(defn column [property-shape]
  (let [name (utils.uri/iri-local-name
              (get-in property-shape
                      [:sh/path :id :id]))]
    [name :varchar 255 [:not nil]]))

(defn table [node-shape]
  (let [target-class (utils.uri/iri-local-name
                      (get-in node-shape
                              [:sh/targetClass :id :id]))]

    (sql/format (h/create-table target-class
                                (h/with-columns (cons
                                                 (id-column)
                                                 (map column (graph.shacl/properties node-shape)))))
                :pretty true)))