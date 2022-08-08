; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.utils.uri
  (:require [ont-app.vocabulary.core :as vocab]) 
  (:import (java.net URI)))

(defn iri-local-name [kw]
  (->> (vocab/uri-for kw)
       URI.
       .getFragment
       keyword))
