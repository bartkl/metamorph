(ns schema-transformer.core
  (:require [cli-matic.core :refer [run-cmd]]
            [deercreeklabs.lancaster :as l]
            [asami.core :as d]
            [clojure.java.io :as io]
            [schema-transformer.reading :as rdf]
            [ont-app.vocabulary.core :as vocab]
            [schema-transformer.schemas.avro.schema :refer [avro-schema]]
            [schema-transformer.graph.db :as graph.db]
            [schema-transformer.vocabs.prof :as prof]
            [schema-transformer.cli :as cli]))

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

(comment "Playground."
         (def db-uri "asami:mem://profile")
         (d/create-database db-uri)
         (d/delete-database db-uri)

         (def conn (d/connect db-uri))

         (def model
           (rdf/read-directory (io/file "resources/example_profile/")))

         (take 20 model)

         @(d/transact conn {:tx-triples model})

         (mark-resources-as-entities conn)

         (def a-shape (d/entity conn (vocab/keyword-for "https://w3id.org/schematransform/ExampleShape#BShape") true))
         (def s (avro-schema a-shape))
         (l/edn s)

         (spit "testBShape.json" (l/json s)))