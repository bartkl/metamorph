; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.utils.uri
  (:require [ont-app.vocabulary.core :as vocab]
            [clojure.string :as string])
  (:import (java.net URI)))

(defn path [iri]
  (map keyword
       (-> (vocab/uri-for iri)
           URI.
           .getPath
           (string/split #"/")
           rest)))

(defn fragment [iri]
  {:pre [(keyword? iri)]}

  (->> (vocab/uri-for iri)
       URI.
       .getFragment
       keyword))

(defn name [iri]
  (keyword
   (or (fragment iri) (last (path iri)))))