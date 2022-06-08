(ns schema-transformer.core
  (:require [cli-matic.core :refer [run-cmd]]
            [clojure.spec.alpha :as spec]
            [expound.alpha :as expound]))

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




(defn -main
  "This is our entry point.
  Just pass parameters and configuration.
  Commands (functions) will be invoked as appropriate."
  [& args]
  (run-cmd args cli-conf))