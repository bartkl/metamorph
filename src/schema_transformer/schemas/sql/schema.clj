(ns schema-transformer.schemas.sql.schema
  (:require [clojure.string :as string]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]
            [schema-transformer.graph.shacl :as graph.shacl]
            [schema-transformer.schemas.sql.datatype :refer [xsd->sql]]))

(defn id-column [] [:id [:varchar 255] [:not nil] [:primary-key]])
(defn- key-ref [t] (keyword (str t "-" "id")))

(defn foreign-key-column [node-shape]
  (let [name (graph.shacl/class-name node-shape)]
    [(key-ref name)]))

(defn primitive-column [property-shape])

(declare table)

(defn- column [property-shape]
  (let [name (graph.shacl/class-name (:sh/path property-shape))
        type (condp #(%1 %2) property-shape
               :sh/datatype :>> xsd->sql
               :sh/node :>> foreign-key-column
               nil)]

    [name type]))

(defn table [node-shape]
  (h/create-table
   (graph.shacl/class-name (node-shape :sh/targetClass))
   (h/with-columns (cons
                    (id-column)
                    (map column (graph.shacl/properties node-shape))))))

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

(comment
  (def test-table
    {:create-table
     [:B
      {:with-columns
       [[:id [:varchar 255] [:not nil] [:primary-key]]
        [:bcd [:double 63 6]]
        [:FromBtoD :DShape]
        [:FromBtoDButSomehowDifferent :DShape]
        [:id [:varchar 255]]]}]}))

  ;; (schema [test-table test-table]))


