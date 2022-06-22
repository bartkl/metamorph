(ns schema-transformer.schemas.sql.schemanew
  (:require [clojure.string :as string]
            [honey.sql :as sql]
            [schema-transformer.rdf.datatype :refer [rdf-list->seq]]
            [honey.sql.helpers :as h]
            [schema-transformer.graph.shacl :as shacl]
            [schema-transformer.schemas.sql.datatype :refer [xsd->sql]]))

(defn normalized-min-count [p]
  (min 1 (p :sh/minCount 0)))

(defn normalized-max-count [p]
  (let [max-count (p :sh/maxCount ##Inf)]
    (if (> 1 max-count)
      :*
      max-count)))

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
                                 [:foreign-key] [:references (table-name (p :sh/node)) (if (fkey? p) (property-name (fkey p)) :value)])
    (= 0 (normalized-min-count p)) (conj [:null])))

(defn ->table [n]
  (h/create-table (table-name n)
                  (h/with-columns (map ->column
                                       (filter #(= 1 (normalized-max-count %))
                                               (shacl/properties n))))))

(defn ->ddl [& ns]
;; enums
;; tables (nodeshapes filteren op `not enum?`)
;; link tables (props met maxCount > 1)
  )