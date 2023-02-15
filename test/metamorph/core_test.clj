; SPDX-FileCopyrightText: 2022 - 2023 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.core-test
  (:require [cli-matic.core :refer [run-cmd*]]
    [kaocha.repl :as k]
    [cheshire.core :as json]
    [tick.core :as t]
    [tick.locale-en-us]
    [deercreeklabs.lancaster :as l]
    [clojure.java.io :as io]
    [clojure.test :refer [deftest is testing]]
    [metamorph.core :as SUT])
  )

(defn- dts []
  (t/format "YYYYMMddHHmm" (t/zoned-date-time)))

(deftest avro-schema-generation
  (testing "Generation of Avro schema [CLI]"
    (testing "from DX Profile"
      (testing "to JSON file [default location]"
        (let [fname "./avro.json"]
          (is (=
                (run-cmd* SUT/command-spec ["--dx-profile" "dev-resources/example_profile" "avro"])
                {:retval 0, :status :OK, :help nil, :subcmd [], :stderr []})
            )
          (is (=
                (slurp "./test/metamorph/avro_success.json")
                (slurp fname)))
          (io/delete-file fname)))
      
      (testing "to JSON file [specified location]"
        (let [fname (str "./" (dts) "-avro.json")]
          (is (=
                (run-cmd* SUT/command-spec ["--dx-profile" "dev-resources/example_profile" "avro" "--output" fname])
                {:retval 0, :status :OK, :help nil, :subcmd [], :stderr []})
            )
          (is (=
                (l/pcf (l/json->schema (slurp "./test/metamorph/avro_success.json")))
                (l/pcf (l/json->schema (slurp fname)))))
          (io/delete-file fname)))))

  (testing "Schema functions properly"
    (run-cmd* SUT/command-spec ["--dx-profile" "dev-resources/example_profile" "avro"])
    (let [schema (l/json->schema (slurp "avro.json"))
          encoded (l/serialize schema {:from-ato-c :individual-1
                                       :from-bto-d-but-somehow-different {:id "D"}
                                       :id "B"
                                       :bcd [1.0 2.0]})
          decoded (l/deserialize schema schema encoded)
          expected (clojure.edn/read-string (slurp "test/metamorph/avro_message.edn"))]
      (is (= expected decoded))
      (io/delete-file "avro.json"))))

(comment
  (run-cmd* SUT/command-spec ["--dx-profile" "dev-resources/example_profile" "avro"])
  (let [schema (l/json->schema (slurp "avro.json"))
        message {:from-ato-c :individual-1
                 :from-bto-d-but-somehow-different {:id "D"}
                 :id "B"
                 :bcd [1.0 2.0]}
        encoded (l/serialize schema message)
        decoded (l/deserialize schema schema encoded)]
    (is (= message decoded))))

(comment
  (k/run-all))
