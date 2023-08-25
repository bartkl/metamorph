; SPDX-FileCopyrightText: 2023 Bart Kleijngeld
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.utils.cli)

(defn kw->opt [kw]
  (str "--" (name kw)))
