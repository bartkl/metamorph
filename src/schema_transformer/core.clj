(ns schema-transformer.core
  (:require [cli-matic.core :refer [run-cmd]]
            [honey.sql :as sql]
            [clojure.spec.alpha :as spec]
            [expound.alpha :as expound]
            [deercreeklabs.lancaster :as l]
            [asami.core :as d]
            [clojure.java.io :as io]
            [schema-transformer.rdf.reading :as rdf]
            [ont-app.vocabulary.core :as vocab]
            [schema-transformer.schemas.avro.schema :refer [avro-schema]]
            [schema-transformer.schemas.sql.schema :as sql.schema]
            [schema-transformer.graph.shacl :as graph.shacl]
            [schema-transformer.graph.db :as graph.db]
            [schema-transformer.vocabs.prof :as prof]
            [schema-transformer.vocabs.role :as role]
            [schema-transformer.cli :as cli]))

;; To run this, try from the project root:
;; ./toycalc-nosub.clj -a 1 -b 80

(defn transform-schema [& args]
  (println args))

(expound/def ::avro-args
  (fn [&args] false) "Erreur args to avro")

(def cli-conf
  {:app {:command "schema-transformer"
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

(defn mark-resources-as-entities [conn]
  @(d/transact conn {:tx-triples
                     (mapcat #(list
                               (graph.db/mark-entity %)
                               (graph.db/add-id %))
                             (graph.db/get-resources conn))}))

(defn -main
  "This is our entry point.
  Just pass parameters and configuration.
  Commands (functions) will be invoked as appropriate."
  [& args]

  ;; (run-cmd args cli/conf))
  (let [db-uri "asami:mem://profile"
        data (rdf/read-directory (io/file "resources/example_profile/"))]
    (d/create-database db-uri)

    (let [conn (d/connect db-uri)]
      data
      @(d/transact conn {:tx-triples data})
      (mark-resources-as-entities conn)

      (->> (d/entity conn (vocab/keyword-for "https://w3id.org/schematransform/ExampleShape#BShape") true)
           (avro-schema)
           (l/edn)))))

(comment
  (def db-uri "asami:mem://profile")
  (d/create-database db-uri)
  (d/delete-database db-uri)

  (def conn (d/connect db-uri))

  (def model
    (rdf/read-directory (io/file "/home/bartkl/Programming/alliander-opensource/SchemaTransformer/app/src/test/resources/rdfs")))

  (take 20 model)

  @(d/transact conn {:tx-triples model})

  (mark-resources-as-entities conn)

  (def a-shape
    (d/entity conn (vocab/keyword-for
                    "https://w3id.org/schematransform/ExampleShape#AShape") true))
  (def b-shape
    (d/entity conn (vocab/keyword-for
                    "https://w3id.org/schematransform/ExampleShape#BShape") true))
  (def c-shape
    (d/entity conn (vocab/keyword-for
                    "https://w3id.org/schematransform/ExampleShape#CShape") true))

  (def s (avro-schema b-shape))
  (l/edn s)

  (def node-shapes
    (flatten (graph.shacl/get-node-shapes conn)))

  (->> node-shapes
       (map #(d/entity conn % true))
       sql.schema/->schema)

  (def shape-sql (sql.schema/->sql a-shape))
  (sql/format shape-sql)

  (map #(get-in % [:sh/path :id]) (graph.shacl/properties b-shape))

  (->>
   (sql.schema/enum c-shape)
   (map sql/format))


  (spit "testBShape.json" (l/json s)))