; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.utils.uri
  (:require [ont-app.vocabulary.core :as vocab])
  (:import (java.net URI)))

(defn fragment [iri]
  {:pre [(keyword? iri)]
   :post [keyword? %]}

  (->> (vocab/uri-for iri)
       URI.
       .getFragment
       keyword))
