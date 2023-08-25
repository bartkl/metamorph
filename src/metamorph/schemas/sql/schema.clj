; SPDX-FileCopyrightText: 2023 Bart Kleijngeld
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.schemas.sql.schema
  (:require [clojure.string :as string]
    [honey.sql :as sql]
    [honey.sql.helpers :as h]
    [metamorph.graph.db :as graph.db]
    [metamorph.graph.shacl :as shacl]
    [metamorph.rdf.datatype :refer [rdf-list->seq]]
    [metamorph.schemas.sql.datatype :refer [xsd->sql]]
    [metamorph.utils.uri :as utils.uri]))

(defn min-count [p]
  (min 1 (p :sh/minCount 0)))

(defn max-count [p]
  (if (= 1 (p :sh/maxCount)) 1 ##Inf))

(defn node-shape-name [n]
  (utils.uri/name
    (graph.db/entity-id (:sh/targetClass n))))

(defn property-shape-name [p]
  (utils.uri/name
    (graph.db/entity-id (:sh/path p))))

(defn primary-key? [p]
  (= (p :rdfs/comment) "PrimaryKey"))

(defn primary-key [n]
  (some->> (n :sh/property)
    (filter primary-key?)
    first))

(defn foreign-key? [p]
  (let [node-props (get-in p [:sh/node :sh/property])]
    (and (not-empty node-props)
      (some primary-key? node-props))))

(defn foreign-key [p]
  (primary-key (p :sh/node)))

(defn enum? [n]
  (contains? n :sh/in))

(defn enum-members [n]
  (map (comp utils.uri/name
         graph.db/entity-id)
    (rdf-list->seq (:sh/in n))))

(defn node-shape->enum [n]
  (let [create-ddl (h/create-table (node-shape-name n)
                     [:value [:varchar 255] [:primary-key]])
        insert-ddl (-> (h/insert-into (node-shape-name n))
                     (h/values [(into [] (enum-members n))]))]
    [create-ddl insert-ddl]))

(defn property-shape->column [p]
  (letfn [(primitive-type [p] (xsd->sql (p :sh/datatype)))]
    (cond-> [(property-shape-name p)]
      (contains? p :sh/datatype) (conj (primitive-type p))
      (primary-key? p) (conj [:primary-key])
      (contains? p :sh/node) (conj
                               (if (enum? (p :sh/node))
                                 [:varchar 255]
                                 (primitive-type (foreign-key p)))

                               [:foreign-key] [:references (node-shape-name (p :sh/node))
                                               (if (foreign-key? p)
                                                 (property-shape-name (foreign-key p))
                                                 :value)])
      (= 0 (min-count p)) (conj :null)
      (and (not (primary-key? p))
        (> (min-count p) 0)) (conj [:not nil]))))

(defn node-shape->table [n]
  (h/create-table (node-shape-name n)
    (h/with-columns (map property-shape->column
                      (filter #(= 1 (max-count %))
                        (shacl/properties n))))))

(defn node-shape->link-table [left-node p]
  (let [left-table (name (node-shape-name left-node))
        left-datatype (xsd->sql ((primary-key left-node) :sh/datatype))
        left-primary-key (name (property-shape-name (primary-key left-node)))
        left-col (keyword (str left-table "_" left-primary-key))
        right (cond
                (contains? p :sh/datatype) (name (property-shape-name p))
                (contains? p :sh/node) (name (node-shape-name (p :sh/node))))
        right-datatype (cond (contains? p :sh/datatype) (xsd->sql (p :sh/datatype))
                         (contains? p :sh/node) (xsd->sql ((foreign-key p) :sh/datatype)))
        right-primary-key (when (contains? p :sh/node) (name (property-shape-name (foreign-key p))))
        right-col (cond
                    (contains? p :sh/datatype) :value
                    (contains? p :sh/node) (keyword (str right "_" right-primary-key)))]

    (-> (h/create-table (property-shape-name p))
      (h/with-columns
        [[left-col left-datatype [:foreign-key] [:references (keyword left-table) (keyword left-primary-key)]]
         (if (some? right-primary-key)
           [right-col right-datatype [:foreign-key] [:references (keyword right) (keyword right-primary-key)]]
           [right-col right-datatype])
         [[:constraint (keyword (str (name (property-shape-name p)) "_" "pkey"))] [:primary-key left-col right-col]]]))))

(defn node-shape->link-tables [n]
  (reduce conj [] (map #(node-shape->link-table n %)
                    (filter #(> (max-count %) 1)
                      (shacl/properties n)))))

(defn node-shapes->ddl [ns]
  (reduce conj []
    (map #(if (enum? %) (node-shape->enum %)
            ((juxt node-shape->table
               node-shape->link-tables) %))
      ns)))

(defn sql-schema [ns]
  (str
    (->> (node-shapes->ddl ns)
      flatten
      (map #(first (sql/format % {:pretty true})))
      (string/join ";"))
    ";"))
