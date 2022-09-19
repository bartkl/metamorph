; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.core
  (:require [cli-matic.core :refer [run-cmd run-cmd*]]
            [clojure.spec.alpha :as spec]
            [expound.alpha :as expound]
            [metamorph.utils.spec :refer [one-key-of]]
            [metamorph.utils.cli :refer [kw->opt]]
            [clojure.tools.cli :refer [parse-opts]]
            [honey.sql :as sql]
            [deercreeklabs.lancaster :as l]
            [clojure.string :as str]
            [asami.core :as d]
            [clojure.java.io :as io]
            [ont-app.vocabulary.core :as vocab]
            [metamorph.rdf.reading :as rdf]
            [metamorph.schemas.avro.schema :refer [avro-schema]]
            [metamorph.schemas.sql.schema :as sql.schema]
            [metamorph.graph.shacl :as graph.shacl]
            [metamorph.graph.avro :as graph.avro]
            [metamorph.graph.db :as graph.db]
            [metamorph.vocabs.prof :as prof]
            [metamorph.vocabs.role :as role])
  (:gen-class))

(def input-sources #{:shacl :dx-profile})

(spec/def ::command-args
  (one-key-of (vec input-sources)))

(expound/defmsg ::command-args
  (str
   "Please provide exactly one input.\n"
   "Choices:\n\t"
   (str/join "\n\t" (map kw->opt input-sources))))

(defn generate-schema [schema]
  (fn [{:keys [dx-profile shacl]}]
   (let [db-uri "asami:mem://logical-model"
         logical-model (rdf/read-triples (io/file (or dx-profile shacl)))]
     (d/create-database db-uri)
     (let [conn (d/connect db-uri)
           root-uri (graph.avro/root-node-shape-uri conn)]
       (graph.db/store-resources! conn logical-model)
       (case schema
         :avro ((->> (graph.db/resource conn root-uri)
                 (avro-schema)
                 (l/json)
                 (spit "testBShape.json"))))))))

  

(def command-spec
  {:command "metamorph"
   :description "Generate a schema of a variety of formats, from a DX Profile or SHACL model."
   :version "0.1.0"
   :opts [{:as "Input: DX Profile directory"
           :option "dx-profile"
           :type :string}
          {:as "Input: SHACL file"
           :option "shacl"
           :type :string}]
   :subcommands [{:command "avro"
                  :description "Apache Avro schema"
                  :opts [{:as "Serialization format"
                          :default :json
                          :option "format"
                          :short  "f"
                          :type #{:edn :json}}
                         {:as "Output file"
                          :default "./avro.json"
                          :option "output"
                          :short  "o"
                          :type :string}]
                  :runs generate-schema}]
   :spec ::command-args})

(defn -main
  "This is our entry point.
  Just pass parameters and configuration.
  Commands (functions) will be invoked as appropriate."
  [& args]

  (run-cmd command-spec args))

;; Playground.
(comment
  (def db-uri "asami:mem://logical-model")
  (d/create-database db-uri)
  (d/delete-database db-uri)

  (def conn (d/connect db-uri))

  (def model
    (rdf/read-triples (io/file "/home/bartkl/Programming/alliander-opensource/metamorph/dev-resources/example_profile")))
    ;; (rdf/read-triples (io/file "/home/bartkl/Programming/alliander-opensource/SchemaTransformer/app/src/test/resources/rdfs")))

  (take 20 model)
  (graph.db/store-resources! conn model)
  (graph.avro/root-node-shape-uri conn)

  ;; (def b-shape (graph.db/resource conn (vocab/keyword-for "https://w3id.org/schematransform/ExampleShape#BShape")))
  (def b-shape-uri (graph.avro/root-node-shape-uri conn))
  (def b-shape (graph.db/resource conn b-shape-uri))

  ;; Avro.
  (def s (avro-schema b-shape))
  (l/edn s)
  (spit "testBShape.json" (l/json s))

  ;; SQL.
  (def node-shapes-names
    (graph.shacl/get-node-shapes conn))

  (def node-shapes (->> node-shapes-names
                        (map #(d/entity conn % true))))

  (map #(get-in % [:sh/path :id]) (graph.shacl/properties b-shape))
  (sql/format (sql.schema/node-shape->table b-shape))

  (->> (sql.schema/sql-schema node-shapes)
       (spit "testSql.sql"))

  ;; CLI Playground.
  ;; (def args ["--dx-profile" "/home/bartkl/Programming/alliander-opensource/metamorph/dev-resources/example_profile" "avro" "-o" "json"])

  (def x (graph.db/resource conn (graph.avro/root-node-shape-uri conn)))

  (get-in x [:id :id])
  (avro-schema x)

  (def args {:dx-profile "/home/bartkl/Programming/alliander-opensource/metamorph/dev-resources/example_profile", :format :json, :output :json, :_arguments []})
  (generate-schema args)

  (run-cmd* command-spec args))
