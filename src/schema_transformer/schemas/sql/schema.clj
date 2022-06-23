(ns schema-transformer.schemas.sql.schema
  (:require [clojure.string :as string]
            [honey.sql :as sql]
            [schema-transformer.rdf.datatype :refer [rdf-list->seq]]
            [honey.sql.helpers :as h]
            [schema-transformer.graph.shacl :as shacl]
            [schema-transformer.schemas.sql.datatype :refer [xsd->sql]]))

(defn normalized-min-count [p]
  (min 1 (p :sh/minCount 0)))

(defn normalized-max-count [p]
  (if (= 1 (p :sh/maxCount)) 1 ##Inf))

(defn table-name [n]
  (shacl/class-name (n :sh/targetClass)))

(defn property-name [p]
  (shacl/class-name (p :sh/path)))

(defn pkey? [p]
  (= (p :rdfs/comment) "PrimaryKey"))

(defn pkey [n]
  (some->> (n :sh/property)
           (filter pkey?)
           first))

(defn fkey [p]
  (pkey (p :sh/node)))

(defn enum-members [n] (map shacl/class-name
                            (rdf-list->seq (:sh/in n))))

(defn enum? [n] (contains? n :sh/in))


(defn fkey? [p]
  (let [node (or (p :sh/node) {})]
    (and (> (count (node :sh/property)) 0)
         (some pkey? (node :sh/property)))))

;; API.
(defn ->enum [n]
  (let [create-ddl (h/create-table (table-name n)
                                   [:value [:varchar 255] [:primary-key]])
        insert-ddl (-> (h/insert-into (table-name n))
                       (h/values [(into [] (enum-members n))]))]
    [create-ddl insert-ddl]))

(defn ->column [p]
  (cond-> [(property-name p)]
    (contains? p :sh/datatype) (conj (xsd->sql (p :sh/datatype)))
    (pkey? p) (conj [:primary-key])
    (contains? p :sh/node) (conj (if (enum? (p :sh/node))
                                   [:varchar 255]
                                   (xsd->sql ((fkey p) :sh/datatype)))
                                 [:foreign-key] [:references
                                                 (table-name (p :sh/node))
                                                 (if (fkey? p)
                                                   (property-name (fkey p))
                                                   :value)])
    (= 0 (normalized-min-count p)) (conj nil)))

(defn ->table [n]
  (h/create-table (table-name n)
                  (h/with-columns (map ->column
                                       (filter #(= 1 (normalized-max-count %))
                                               (shacl/properties n))))))

(defn ->link-table [left-node p]
  (let [left-table (name (table-name left-node))
        left-datatype (xsd->sql ((pkey left-node) :sh/datatype))
        left-pkey (name (property-name (pkey left-node)))
        left-col (keyword (str left-table "_" left-pkey))
        right (cond
                (contains? p :sh/datatype) (name (property-name p))
                (contains? p :sh/node) (name (table-name (p :sh/node))))
        right-datatype (cond (contains? p :sh/datatype) (xsd->sql (p :sh/datatype))
                             (contains? p :sh/node) (xsd->sql ((fkey p) :sh/datatype)))
        right-pkey (when (contains? p :sh/node) (name (property-name (fkey p))))
        right-col (cond
                    (contains? p :sh/datatype) :value
                    (contains? p :sh/node) (keyword (str right "_" right-pkey)))]

    (-> (h/create-table (property-name p))
        (h/with-columns
          [[left-col left-datatype [:foreign-key] [:references (keyword left-table) (keyword left-pkey)]]
           [right-col right-datatype (when (some? right-pkey)
                                       [:foreign-key]
                                       [:references (keyword right) (keyword right-pkey)])]
           [[:constraint (keyword (str (name (property-name p)) "_" "pkey"))] [:primary-key left-col right-col]]]))))

(defn ->link-tables [n]
  (reduce conj [] (map #(->link-table n %)
                       (filter #(> (normalized-max-count %) 1)
                               (shacl/properties n)))))

(defn ->ddl [ns]
  (reduce conj [] (map #(if (enum? %) (->enum %)
                            ((juxt ->table
                                   ->link-tables) %))
                       ns)))




(defn ->schema [node-shapes]
  (str
   (->> (->ddl node-shapes)
        flatten
        (map #(first (sql/format % {:pretty true})))
        (string/join ";"))
   ";"))