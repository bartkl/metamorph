; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.cli
  (:require [clojure.spec.alpha :as spec]
            [expound.alpha :as expound]))

(defn transform-schema [& args]
  (println args))

(expound/def ::avro-args
  (fn [& args] false) "Erreur args to avro")

(def conf
  {:app {:command "metamorph"
         :description "Tool to transform dx-prof/CIM501 profiles to a variety of schema"
         :version "0.0.1"}
   :global-opts [{:as "Manifest file which describes the changed files since last run"
                  :option "manifest"
                  :short  "m"
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