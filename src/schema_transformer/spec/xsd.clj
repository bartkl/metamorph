; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns schema-transformer.spec.xsd
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]))

;; TODO: Look into ways of doing this programmatically,
;;  perhaps using `specs-for-ns`.
(s/def :xsd/datatype  
  (s/or
   :string string?
   :double double?
   :boolean boolean
   :int int?
   :decimal decimal?
   :float float?
   :duration bytes?
   :dateTime string?
   :date string?
   :time string?
   :anyURI string?))

(s/def :xsd/string string?)
(s/def :xsd/double double?)
(s/def :xsd/boolean boolean?)
(s/def :xsd/int int?)
(s/def :xsd/decimal decimal?)
(s/def :xsd/float float?)
(s/def :xsd/duration bytes?)
(s/def :xsd/dateTime string?)
(s/def :xsd/date string?)
(s/def :xsd/time string?)
(s/def :xsd/anyURI string?)