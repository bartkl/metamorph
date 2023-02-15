; SPDX-FileCopyrightText: 2022 - 2023 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.spec.owl
  (:require [clojure.spec.alpha :as s]))

;; TODO: Implement?
(s/def :owl/Class any?)
(s/def :owl/ObjectProperty any?)
(s/def :owl/DatatypeProperty any?)
