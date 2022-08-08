; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns schema-transformer.spec.reading
  (:require [clojure.spec.alpha :as s]
            [schema-transformer.utils.file :as utils.file]))

(s/fdef schema-transformer.rdf.reading/read-triples-from-file
  :args (s/cat :path utils.file/file?)
  :ret (s/coll-of :rdf/triple))

(s/fdef schema-transformer.rdf.reading/read-triples-from-directory
  :args (s/cat :path utils.file/dir?)
  :ret (s/coll-of :rdf/triple))