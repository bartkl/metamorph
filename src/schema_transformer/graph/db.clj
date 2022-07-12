; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns schema-transformer.graph.db
  (:require [asami.core :as d]))

(defn node-ref? [m]
  (= (keys m) '(:id)))

(defn get-resources [conn]
  (map first
       (d/q '[:find ?subj
              :where [?subj _ _]]
            conn)))

(def mark-entity #(vector % :a/entity true))
(def add-id #(vector % :id %))
