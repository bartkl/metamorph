; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.utils.file
  (:refer-clojure :exclude [type])
  (:require [clojure.java.io :as io])
  (:import (java.io File)))

(defn ext [f]
  (last
   (re-find #"\.([^.\\/:*\"?<>|\r\n]+)$" (.getName f))))

(defn dir? [path] (.isDirectory path))
(defn file? [path] (.isFile path))

(defn type [path]
  (when (instance? File path)
    (if (dir? path)
      :directory
      :file)))
