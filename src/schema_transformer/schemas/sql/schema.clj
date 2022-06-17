(ns schema-transformer.schemas.sql.schema
  (:require [clojure.string :as string]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]
            [schema-transformer.graph.shacl :as shacl]
            [schema-transformer.schemas.sql.datatype :refer [xsd->sql]]))

(defn- key-ref [t]
  (keyword (str (name t) "-" "id")))

(defn- foreign-key-column [node-shape]
  (let [name (shacl/class-name node-shape)]
    [(key-ref name)]))

(declare table)

(defn- pkey? [property-shape]
  (= (property-shape :rdfs/comment) "PrimaryKey"))

(defn- fkey? [property-shape]
  (contains? property-shape :sh/node))

(defn pkey [node-shape]
  (some->> (get node-shape :sh/property)
           (filter pkey?)
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
      (pkey? property-shape) (conj [:primary-key])
      (fkey? property-shape) (conj [:foreign-key]
                                   [:references
                                    (shacl/class-name (get-in property-shape [:sh/node :sh/targetClass]))
                                    (or (pkey (property-shape :sh/node)) :none)]))))


(defn table [node-shape]
  (h/create-table
   (shacl/class-name (node-shape :sh/targetClass))
   (h/with-columns (map column (shacl/properties node-shape)))))

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


