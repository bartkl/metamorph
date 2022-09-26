; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.utils.cli)

(defn kw->opt [kw]
  (str "--" (name kw)))
