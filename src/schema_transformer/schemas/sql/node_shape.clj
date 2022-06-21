(ns schema-transformer.schemas.sql.node-shape
  (:require [clojure.string :as string]
            [clojure.core :as core]
            [honey.sql :as sql]
            [schema-transformer.rdf.datatype :refer [rdf-list->seq]]
            [honey.sql.helpers :as h]
            [schema-transformer.graph.shacl :as shacl]
            [schema-transformer.schemas.sql.datatype :refer [xsd->sql]]))

(declare table)

(defn column-pkey? [property-shape]
  (= (property-shape :rdfs/comment) "PrimaryKey"))

(defn column-fkey? [property-shape]
  (let [node (or (property-shape :sh/node) {})]
    (and (> (count (node :sh/property)) 0)
         (column-pkey? node))))

(defn pkey [node-shape]
  (some->> (get node-shape :sh/property)
           (filter column-fkey?)
           first
           :sh/path
           shacl/class-name))

(defn enum-column [node-shape]
  (let [name (shacl/class-name (node-shape :sh/targetClass))]
    [name [:varchar 255] [:primary-key]]))

(defn regular-column [property-shape]
  (let [name (shacl/class-name (:sh/path property-shape))
        type (condp #(%1 %2) property-shape
               :sh/datatype :>> xsd->sql
               :sh/node :>> (fn [_] [:varchar 255])
               nil)]
    (cond-> [name type]
      (column-pkey? property-shape) (conj [:primary-key])
      (column-fkey? property-shape) (conj [:foreign-key]
                                          [:references
                                           (shacl/class-name (get-in property-shape [:sh/node :sh/targetClass]))
                                           (or (pkey (property-shape :sh/node)) :none)]))))

(defn name [n]
  (shacl/class-name (n :sh/targetClass)))

(defn ->table [n]
  (h/create-table
   (name n)
   (h/with-columns (map regular-column (shacl/properties node-shape)))))

(defn enum [node-shape]
  (let [name (shacl/class-name (node-shape :sh/targetClass))
        members (map shacl/class-name (rdf-list->seq (:sh/in node-shape)))
        create-ddl (h/create-table name [:value [:varchar 255] [:primary-key]])
        insert-ddl (-> (h/insert-into name)
                       (h/values [(into [] members)]))]
    (list create-ddl insert-ddl)))

(defn enum? [node-shape] (contains? node-shape :sh/in))

(defn table [node-shape]
  (if (enum? node-shape)

    (enum node-shape)
    (->table node-shape)))

;; (defn schema [node-shapes]
;;   ;; (reduce (partial string/join ";") (->> node-shapes
;;   ;;                                        (map table)
;;   ;;                                        (sql/format)))

;;   (->> node-shapes
;;        (map table)
;;        (sql/format)
;;        (reduce #(string/join ";" %))))

  ;; (loop [tbl (table node-shape)
  ;;        tables {}]
  ;;   ((second (tbl :create-table)) :with-columns)
  ;;   ))