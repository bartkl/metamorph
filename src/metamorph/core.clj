; SPDX-FileCopyrightText: 2023 Bart Kleijngeld
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.core
  (:require [cli-matic.core :refer [run-cmd run-cmd*]]
            [clojure.spec.alpha :as spec]
            [expound.alpha :as expound]
            [metamorph.utils.cli :refer [kw->opt]]
            [deercreeklabs.lancaster :as l]
            [asami.core :as d]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [cheshire.generate :refer [add-encoder]]
            [cheshire.core :as json]
            [metamorph.rdf.reading :as rdf]
            [metamorph.schemas.avro.schema :refer [avro-schema]]
            [metamorph.graph.avro :as graph.avro]
            [metamorph.graph.db :as graph.db]
            [ont-app.vocabulary.core :as vocab]
            [metamorph.utils.json :refer [encode-keyword]]
            [metamorph.vocabs.prof :as prof]
            [metamorph.vocabs.role :as role])
  (:gen-class))

(def input-sources #{:shacl :dx-profile})

(add-encoder clojure.lang.Keyword encode-keyword)

(expound/defmsg ::command-args
  (str
   "Please provide exactly one input.\n"
   "Choices:\n\t"
   (string/join "\n\t" (map kw->opt input-sources))))

(defn read-input [{:keys [input]}]
  (rdf/read-triples (io/file input)))

(defn store-in-db [data]
  (let [db-uri "asami:mem://logical-model"]
    (d/delete-database db-uri)  ; Ensure clean state.
    (d/create-database db-uri)
    (let [conn (d/connect db-uri)]
      (graph.db/store-resources! conn data)
      conn)))

(defn generate-avro-schema [conn args]
  (let [root-uri (graph.avro/get-root-node-shape-uri conn)]
    (->> (graph.db/get-resource conn root-uri)
         avro-schema
         l/edn
         (json/encode)
         (spit (:output args)))))

(defn generate-schema [schema]
  (fn [args]
    (let [gen-schema (schema {:avro generate-avro-schema})
          conn (->> (read-input args)
                    store-in-db)]
      (gen-schema conn args))))


  (def command-spec
    (let [shared-opts [{:as "Input: DX Profile directory or SHACL file"
                              :option "input"
                              :short 0
                              :type :string}]]
      {:command "metamorph"
       :description "Generate a schema of a variety of formats, from a DX Profile or SHACL model."
       :version "0.3.0"
       :opts []
       :subcommands [{:command "avro"
                      :description "Apache Avro schema"
                      :opts (into [] (concat shared-opts
                                             [{:as "Serialization format"
                                               :default :avsc
                                               :option "format"
                                               :short  "f"
                                               :type #{:edn :json :avsc}}
                                              {:as "Output file"
                                               :default "./schema.avsc"
                                               :option "output"
                                               :short 1
                                               :type :string}]))
                      ;; :runs println}]
                      :runs (generate-schema :avro)}]
     }))

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
    (rdf/read-triples (io/file "dev-resources/example_profile")))

  (take 20 model)

  (graph.db/store-resources! conn model)
  (graph.avro/get-root-node-shape-uri conn)

  ; (def b-shape (graph.db/get-resource conn (vocab/keyword-for "https://w3id.org/schematransform/ExampleShape#BShape")))
  (def b-shape-uri (graph.avro/get-root-node-shape-uri conn))
  (def b-shape (graph.db/get-resource conn b-shape-uri))

  ;; Avro.
  (def s (avro-schema b-shape))
  (l/edn s)
  ;; (json/generate-string (l/edn s))
  (spit "testBShape.json" (json/encode (l/edn s)))

  (def root (graph.db/get-resource conn (graph.avro/get-root-node-shape-uri conn)))

  (get-in root [:id :id])
  (avro-schema root)

  ;; CLI Playground.
  (def args ["avro" "dev-resources/example_profile"])
  (run-cmd* command-spec args)

  (def args {:input "dev-resources/example_profile",
             :format :avsc, :output "./schema.avsc", :_arguments ["dev-resources/example_profile"]})
  ((generate-schema :avro) args)  ; Debugging possible exceptions is easier this way than through `run-cmd*`.

  ;; SQL.
  (require '[honey.sql :as sql]
           '[metamorph.schemas.sql.schema :as sql.schema]
           '[metamorph.graph.shacl :as graph.shacl])
  (def node-shapes-names
    (graph.shacl/get-node-shapes conn))

  (def node-shapes (->> node-shapes-names
                        (map #(d/entity conn % true))))

  (map #(get-in % [:sh/path :id]) (graph.shacl/properties b-shape))
  (sql/format (sql.schema/node-shape->table b-shape))

  (->> (sql.schema/sql-schema node-shapes)
       (spit "testSql.sql")))
