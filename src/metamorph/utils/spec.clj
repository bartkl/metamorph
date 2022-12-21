; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.utils.spec
  (:require [clojure.set :as set]))

(defn one-key-of [kws]
  (fn [m]
    (= (count (set/intersection (into #{} kws)
                                (into #{} (keys m))))
       1)))
