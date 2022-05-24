(ns schema-transformer.core
  (:require [cli-matic.core :refer [run-cmd]]
            [asami.core :as d]
            [clojure.java.io :as io]
            [schema-transformer.rdf :as rdf]
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
        conn #(d/connect db-uri)]
    (do
      (as-> (rdf/read-directory (io/file "resources/example_profile/")) v
        @(d/transact conn {:tx-triples v}))
      (mark-resources-as-entities conn))))




(comment
  (def db-uri "asami:mem://profile")
  (d/create-database db-uri)
  (d/delete-database db-uri)

  (def conn (d/connect db-uri))

  (def model
    (rdf/read-directory (io/file "resources/example_profile/")))

  (take 2 model)

  @(d/transact conn {:tx-triples model})

  (mark-resources-as-entities conn)


  (def start-node (d/entity conn (vocab/keyword-for "https://w3id.org/schematransform/ExampleShape#BShape") true))
;;  (def a-shape (d/entity conn :https://w3id.org/schematransform/ExampleShape#AShape true))
;;  (def d-shape (d/entity conn :https://w3id.org/schematransform/ExampleShape#DShape true))
;;  (def root-node start-node)
;;  (map #(get-in % [:sh/path]) (get-inherited-props a-shape))

  (def a (avro-schema start-node))
  (l/edn a)

;;  (spit "testBShape.json" (l/json a))
  )