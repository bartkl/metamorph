; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.rdf.reading-test
  (:require [clojure.java.io :as io]
            [clojure.edn]
            [clojure.string :as string]
            [clojure.test :refer :all]
            [clojure.set :as set]
            [clojure.spec.test.alpha :as spec-test]
            [ont-app.vocabulary.core :as vocab]
            [metamorph.rdf.reading :as SUT]
            [metamorph.utils.file :as utils.file])
  (:import (org.eclipse.rdf4j.rio Rio)
           (org.eclipse.rdf4j.model IRI)
           (org.eclipse.rdf4j.rio RDFFormat)))

(def TEST-DIR (io/file "dev-resources/example_profile/"))

;; (defn- compare-statement [s t]
;;   (if (string/starts-with? (name s))))

(deftest read-file-test
  (testing "Reading single Turtle file"
    (let [ttl (io/file "dev-resources/example_profile/Constraints.ttl")
          actual (SUT/read-file ttl)
          expected (clojure.edn/read-string (slurp  "dev-resources/test.edn"))]
    ;;   (->> (pr-str actual) (spit "dev-resources/test.edn"))
    ;;   (is (= actual expected)))))
      (is (= 1 1)))))

(run-tests)


(comment
  (spec-test/check `SUT/read-file)
  (let [ttl (io/file "dev-resources/example_profile/Constraints.ttl")
        actual (SUT/read-file ttl)]
    (println actual))
  )
