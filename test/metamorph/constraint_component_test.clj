; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.constraint-component-test
  (:require
    [clojure.test :refer :all]
    [ont-app.vocabulary.core :as vocab]
    [metamorph.constraint-component :as cc]
    [metamorph.utils.file :as utils.file]))

(deftest and-constraint-component-test
  (testing "N sh:and M"
    (let [N {:sh/property
             [{:sh/path :rdfs/comment
               :sh/minCount 1}]}
          M {:sh/property
             [{:sh/path :rdfs/label
               :sh/maxCount 2}]}]
      (cc/and-constraint-component)
      )))

(comment "N sh:and M"
  (let [N {:sh/property
           [{:sh/path :rdfs/comment
             :sh/minCount 1}]}
        M {:sh/property
           [{:sh/path :rdfs/label
             :sh/maxCount 2}]}]
    (cc/and-constraint-component [N M])
    ))