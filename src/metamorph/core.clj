; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.core
  (:require [cli-matic.core :refer [run-cmd]]
            [honey.sql :as sql]
            [deercreeklabs.lancaster :as l]
            [asami.core :as d]
            [clojure.java.io :as io]
            [ont-app.vocabulary.core :as vocab]
            [metamorph.cli :as cli]
            [metamorph.rdf.reading :as rdf]
            [metamorph.schemas.avro.schema :refer [avro-schema]]
            [metamorph.schemas.sql.schema :as sql.schema]
            [metamorph.graph.shacl :as graph.shacl]
            [metamorph.graph.db :as graph.db]
            [metamorph.vocabs.prof :as prof]
            [metamorph.vocabs.role :as role]
            [metamorph.cli :as cli])
  (:gen-class))


(defn -main
  "This is our entry point.
  Just pass parameters and configuration.
  Commands (functions) will be invoked as appropriate."
  [& args]

  (println args)  ;; NOTE: Remove when done with developing.
  (run-cmd args cli/command-spec))

  ;; TODO: Implement logic that, dependent on the CLI args, performs the schema generation.
  ;; Example for Avro:
  ;;
  ;; (let [db-uri "asami:mem://profile"
  ;;       data (rdf/read-triples (io/file "resources/example_profile/"))]
  ;;   (d/create-database db-uri)
  ;;
  ;;   (let [conn (d/connect db-uri)]
  ;;     data
  ;;     @(d/transact conn {:tx-triples data})
  ;;     (graph.db/mark-resources-as-entities conn)
  ;;
  ;;     (->> (d/entity conn (vocab/keyword-for "https://w3id.org/schematransform/ExampleShape#BShape") true)
  ;;          (avro-schema)
  ;;          (l/edn)))))

(comment
  (def db-uri "asami:mem://profile")
  (d/create-database db-uri)
  (d/delete-database db-uri)

  (def conn (d/connect db-uri))

  (def model
    (rdf/read-triples (io/file "/home/bartkl/Programming/alliander-opensource/SchemaTransformer/app/src/test/resources/rdfs")))

  (take 20 model)
  (graph.db/store-resources! conn model)

  (def b-shape (graph.db/resource conn (vocab/keyword-for "https://w3id.org/schematransform/ExampleShape#BShape")))

  ;; Avro  .
  (def s (avro-schema b-shape))
  (l/edn s)
  (spit "testBShape.json" (l/json s))

  ;; SQL.
  (def node-shapes-names
    (flatten (graph.shacl/get-node-shapes conn)))

  (def node-shapes (->> node-shapes-names
                        (map #(d/entity conn % true))))

  (map #(get-in % [:sh/path :id]) (graph.shacl/properties b-shape))
  (sql/format (sql.schema/node-shape->table b-shape))

  (->> (sql.schema/sql-schema node-shapes)
       (spit "testSql.sql")))
