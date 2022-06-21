(ns schema-transformer.schemas.sql.schema
  (:require [clojure.string :as string]
            [honey.sql :as sql]
            [schema-transformer.rdf.datatype :refer [rdf-list->seq]]
            [honey.sql.helpers :as h]
            [schema-transformer.graph.shacl :as shacl]
            [schema-transformer.schemas.sql.datatype :refer [xsd->sql]]))

(declare table-name)

;; (defn key-ref [t]
;;   (keyword (str (name t) "-" "id")))

(defn column-pkey? [property-shape]
  (= (property-shape :rdfs/comment) "PrimaryKey"))

(defn column-fkey? [property-shape]
  (let [node (or (property-shape :sh/node) {})]
    (and (> (count (node :sh/property)) 0)
         (some column-pkey? (node :sh/property)))))

(defn pkey [node-shape]
  (some->> (get node-shape :sh/property)
           (filter column-pkey?)
           first
           :sh/path
           shacl/class-name))

(defn column [property-shape]
  (let [name (shacl/class-name (:sh/path property-shape))
        type (condp #(%1 %2) property-shape
               :sh/datatype :>> xsd->sql
               :sh/node :>> (fn [_] [:varchar 255])
               nil)]
    (cond-> [name type]
      (column-pkey? property-shape) (conj [:primary-key])
      (column-fkey? property-shape) (conj [:foreign-key]
                                          [:references
                                           (table-name (property-shape :sh/node))
                                           (or (pkey (property-shape :sh/node)) :none)]))))

(defn ->table [node-shape]
  (h/create-table
   (table-name node-shape)
   (h/with-columns (map column (shacl/properties node-shape)))))

(defn table-name [node-shape]
  (shacl/class-name (node-shape :sh/targetClass)))

(defn ->enum [node-shape]
  (let [name (table-name node-shape)
        enum-members (map shacl/class-name (rdf-list->seq (:sh/in node-shape)))
        create-ddl (h/create-table name [:value [:varchar 255] [:primary-key]])
        insert-ddl (-> (h/insert-into name)
                       (h/values [(into [] enum-members)]))]
    [create-ddl insert-ddl]))

(defn enum? [node-shape] (contains? node-shape :sh/in))

(defn ->sql [node-shape]
  (if (enum? node-shape)
    (->enum node-shape)
    (->table node-shape)))

(defn ->schema [node-shapes]
  (str
   (->> node-shapes
        (map ->sql)
        flatten
        (map #(first (sql/format % {:pretty true})))
        (string/join ";"))
   ";"))