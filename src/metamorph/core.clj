; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.core
  (:require [cli-matic.core :refer [run-cmd]]
            [honey.sql :as sql]
            [clojure.spec.alpha :as spec]
            [expound.alpha :as expound]
            [deercreeklabs.lancaster :as l]
            [asami.core :as d]
            [clojure.java.io :as io]
            [ont-app.vocabulary.core :as vocab]
            [metamorph.rdf.reading :as rdf]
            [metamorph.schemas.avro.schema :refer [avro-schema]]
            [metamorph.schemas.sql.schema :as sql.schema]
            [metamorph.graph.shacl :as graph.shacl]
            [metamorph.graph.db :as graph.db]
            [metamorph.vocabs.prof :as prof]
            [metamorph.vocabs.role :as role]
            [metamorph.cli :as cli]))

(defn transform-schema [& args]
  (println args))

(expound/def ::avro-args
  (fn [&args] false) "Erreur args to avro")

(def cli-conf
  {:app {:command "metamorph"
         :description "Tool to transform dx-prof/CIM501 profiles to a variety of schema"
         :version "0.0.1"}
   :global-opts [{:as "Manifest file which describes the changed files since last run"
                  :option "manifest"
                  :short  "m"
                  :type :string}
                 {:as "Manifest file which describes the changed files since last run"
                  :option "profile"
                  :short  "p"
                  :type :string}
                 {:as "Base path for the relative file paths in the manifest file"
                  :option "base-path"
                  :short "b"
                  :type :string}]
   :commands [{:command "avro"
               :spec ::avro-args
               :description "Apache AVRO schema"
               :opts [{:as "Serialization format"
                       :default :json
                       :option "format"
                       :short  "f"
                       :type #{:edn :json}}
                      {:as "Output file"
                       :default "./avro.json"
                       :option "output"
                       :short  "o"
                       :type #{:edn :json}}]

               :runs transform-schema}]})

(defn -main
  "This is our entry point.
  Just pass parameters and configuration.
  Commands (functions) will be invoked as appropriate."
  [& args]
  identity)

  ;; (run-cmd args cli/conf))
  ;; (let [db-uri "asami:mem://profile"
  ;;       data (rdf/read-directory (io/file "resources/example_profile/"))]
  ;;   (d/create-database db-uri)

  ;;   (let [conn (d/connect db-uri)]
  ;;     data
  ;;     @(d/transact conn {:tx-triples data})
  ;;     (graph.db/mark-resources-as-entities conn)

  ;;     (->> (d/entity conn (vocab/keyword-for "https://w3id.org/schematransform/ExampleShape#BShape") true)
  ;;          (avro-schema)
  ;;          (l/edn)))))

(comment
  (def db-uri "asami:mem://profile")
  (d/create-database db-uri)
  (d/delete-database db-uri)

  (def conn (d/connect db-uri))

  (def model
    (rdf/read-directory (io/file "/home/bartkl/Programming/alliander-opensource/SchemaTransformer/app/src/test/resources/rdfs")))

  (take 20 model)

  (graph.db/store-resources! conn model)

  (def b-shape (graph.db/resource conn (vocab/keyword-for "https://w3id.org/schematransform/ExampleShape#BShape")))
  (def c-shape (graph.db/resource conn (vocab/keyword-for "https://w3id.org/schematransform/ExampleShape#CShape")))
  (def s (avro-schema b-shape))
  (l/edn s)

  (def node-shapes-names
    (flatten (graph.shacl/get-node-shapes conn)))

  (def node-shapes (->> node-shapes-names
                        (map #(d/entity conn % true))))

  (map #(get-in % [:sh/path :id]) (graph.shacl/properties b-shape))
  (sql/format (sql.schema/->table b-shape))
  ;; (sql.schema/->ddl b-shape c-shape)
  ;; (->> (sql.schema/->schema [b-shape]) (spit "testSql.sql"))
  (->> (sql.schema/node-shapes->schema node-shapes) (spit "testSql.sql"))
  (map #(get % :create-table) (sql.schema/->schema node-shapes))

  (->>
   (sql.schema/->enum c-shape)
   (map sql/format))


  (spit "testBShape.json" (l/json s)))