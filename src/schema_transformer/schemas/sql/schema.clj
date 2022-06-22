(ns schema-transformer.schemas.sql.schema
  (:require [clojure.string :as string]
            [honey.sql :as sql]
            [schema-transformer.rdf.datatype :refer [rdf-list->seq]]
            [honey.sql.helpers :as h]
            [schema-transformer.graph.shacl :as shacl]
            [schema-transformer.schemas.sql.datatype :refer [xsd->sql]]))

(defn table-name [n]
  (shacl/class-name (n :sh/targetClass)))

(defn property-name [p]
  (shacl/class-name (p :sh/path)))

(defn pkey-name [p] (property-name p))

;; (defn key-ref [t]
;;   (keyword (str (name t) "-" "id")))

(defn pkey? [p]
  (= (p :rdfs/comment) "PrimaryKey"))

(defn fkey? [p]
  (let [node (or (p :sh/node) {})]
    (and (> (count (node :sh/property)) 0)
         (some pkey? (node :sh/property)))))

(defn pkey [n]
  (some->> (n :sh/property)
           (filter pkey?)
           first))

(defn fkey [p]
  (pkey (p :sh/node)))

(defn- pkey-col [p]
  (let [name (pkey-name p)
        type (xsd->sql (p :sh/datatype))]
    [name type [:primary-key]]))

(defn ->column [p]
  (let [type (condp #(%1 %2) p
               :sh/datatype :>> xsd->sql
               :sh/node :>> (fn [_] [:varchar 255])
               nil)]
    (cond-> [(pkey-name p) type]
      (pkey? p) (conj [:primary-key])
      (fkey? p) (conj [:foreign-key]
                      [:references
                       (table-name (p :sh/node))
                       (or (pkey (p :sh/node)) :none)]))))

(defn ->table [n]
  (h/create-table
   (table-name n)
   (h/with-columns [(pkey-col (pkey n))])))

(defn ->enum-table [n]
  (let [name (table-name n)
        enum-members (map shacl/class-name (rdf-list->seq (:sh/in n)))
        create-ddl (h/create-table name [:value [:varchar 255] [:primary-key]])
        insert-ddl (-> (h/insert-into name)
                       (h/values [(into [] enum-members)]))]
    [create-ddl insert-ddl]))

(defn enum? [n] (contains? n :sh/in))

(defn process-node-shape [n]
  (if (enum? n)
    (->enum-table n)
    (->table n)))

(defn normalized-min-count [p]
  (min 1 (p :sh/minCount 0)))

(defn normalized-max-count [p]
  (let [max-count (p :sh/maxCount ##Inf)]
    (if (> 1 max-count)
      :*
      max-count)))

(defn ->link-table [table-name p])

(defn add-col [tbl-name p nullable?]
  (-> (h/alter-table tbl-name)
      (map h/add-column )))

(defn process-property-shape [n p]
  (let [min-count (normalized-min-count p)
        max-count (normalized-max-count p)
        tbl (table-name n)
        type []]
    (case [min-count max-count type]
      [0  1] (add-col tbl p true)
      [1  1] (add-col tbl p false)
      [0 :*] (->link-table tbl p)
      [1 :*] (->link-table tbl p))))
      ;; [0  1 :sh/datatype] (add-col tbl p nil true)  
      ;; [1  1 :sh/datatype] (add-col tbl p nil false)   
      ;; [0 :* :sh/datatype] (->link-table tbl p nil)
      ;; [1 :* :sh/datatype (->link-table tbl p nil)])))










(defn schema-reducer [ddl n]
  (flatten
   (conj ddl
         (process-node-shape n)
         (map #(process-property-shape n %)
              (filter #(not (pkey? %)) (shacl/properties n))))))

(defn ->schema [node-shapes]
  (str
   (->> node-shapes
        (map ->table)
        flatten
        (map #(first (sql/format % {:pretty true})))
        (string/join ";"))
   ";"))