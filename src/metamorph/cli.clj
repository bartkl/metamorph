(ns metamorph.cli
  (:require [cli-matic.core :refer [run-cmd]]
            [clojure.string :as str]
            [clojure.spec.alpha :as spec]
            [expound.alpha :as expound]
            [metamorph.utils.spec :refer [one-key-of]]))

(defn transform-schema [& args]
  (println args))

(def input-sources #{:shacl-file :dx-profile})

(spec/def ::command-args
  (one-key-of (vec input-sources)))

(expound/defmsg ::command-args
  (str
   "Please provide exactly one input.\n"
   "Choices:\n\t"
   (str/join "\n\t" (map #(str "--" (name %)) input-sources))))

(def command-spec
  {:command "metamorph"
   :description "Generate a schema of a variety of formats from a DX Profile or SHACL model."
   :version "0.0.1"
   :opts [{:as "Input: DX Profile directory"
           :option "dx-profile"
           :type :string}
          {:as "Input: SHACL file"
           :option "shacl-file"
           :type :string}]
   :subcommands [{:command "avro"
                  :spec ::avro-args
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
                          :type #{:edn :json}}]

                  :runs transform-schema}]
   :spec ::command-args})
