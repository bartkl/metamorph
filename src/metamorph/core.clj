; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.core
  (:require [cli-matic.core :refer [run-cmd run-cmd*]]
    [clojure.spec.alpha :as spec]
    [expound.alpha :as expound]
    [metamorph.utils.spec :refer [one-key-of]]
    [metamorph.utils.cli :refer [kw->opt]]
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

(defn read-input [{:keys [dx-profile shacl]}]
  (rdf/read-triples (io/file (or dx-profile shacl))))

(defn store-in-db [data]
  (let [db-uri "asami:mem://logical-model"]
    (d/delete-database db-uri)  ;; Ensure clean state.
    (d/create-database db-uri)
    (let [conn (d/connect db-uri)]
      (graph.db/store-resources! conn data)
      conn)))

(defn generate-avro-schema [conn args]
  (let [root-uri (graph.avro/get-root-node-shape-uri conn)]
    (->> (graph.db/get-resource conn root-uri)
      avro-schema
      l/json
      (spit (:output args)))))

(defn generate-schema [schema]
  (fn [args]
    (let [gen-schema (schema {:avro generate-avro-schema})
          conn (->> (read-input args)
                 store-in-db)]
      (gen-schema conn args))))

(def command-spec
  {:command "metamorph"
   :description "Generate a schema of a variety of formats, from a DX Profile or SHACL model."
   :version "0.3.0"
   :opts [{:as "Input: DX Profile directory"
           :option "dx-profile"
           :type :string}]
          ; {:as "Input: SHACL file"
          ;  :option "shacl"
          ;  :type :string}]
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
                  :runs (generate-schema :avro)}]
   :spec ::command-args})

(defn -main
  "This is our entry point.
  Just pass parameters and configuration.
  Commands (functions) will be invoked as appropriate."
  [& args]

  (run-cmd args command-spec))

(comment  ;; Playground.
  ;; Reading from files into DB.
  (def db-uri "asami:mem://test-db")
  (d/create-database db-uri)
  (d/delete-database db-uri)

  (def conn (d/connect db-uri))
  (def model
    (rdf/read-triples (io/file "/home/bartkl/Programming/alliander-opensource/metamorph/dev-resources/example_profile")))

  (take 20 model)
  (graph.db/store-resources! conn model)
  (graph.avro/get-root-node-shape-uri conn)

  ;; (def b-shape (graph.db/get-resource conn (vocab/keyword-for "https://w3id.org/schematransform/ExampleShape#BShape")))
  (def b-shape-uri (graph.avro/get-root-node-shape-uri conn))
  (def b-shape (graph.db/get-resource conn b-shape-uri))

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
  (def x (graph.db/get-resource conn (graph.avro/get-root-node-shape-uri conn)))

  (get-in x [:id :id])
  (avro-schema x)

  (def args {:dx-profile "dev-resources/example_profile",
             :format :json, :output "./avro.json", :_arguments []})
  ((generate-schema :avro) args)

  (def args ["--dx-profile" "dev-resources/example_profile" "avro"])
  (run-cmd* command-spec args))
