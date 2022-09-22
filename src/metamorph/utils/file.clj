; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.utils.file
  (:refer-clojure :exclude [type])
  (:require [clojure.java.io :as io])
  (:import (java.io File)))

(defn ext [f]
  (last
   (re-find #"\.([^.\\/:*\"?<>|\r\n]+)$" (.getName ^java.io.File f))))

(defn dir? [path] (.isDirectory ^java.io.File path))
(defn file? [path] (.isFile ^java.io.File path))

(defn type [path]
  (when (instance? File path)
    (if (dir? path)
      :directory
      :file)))
