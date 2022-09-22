; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(defproject metamorph "0.3.0"
  :description "Metamorph is a Clojure libary that allows the generation of a variety of schemas from a DX-PROF profile."
  :url "https://github.com/alliander-opensource/metamorph"
  :license {:name "Apache License 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :plugins [[lein-cljfmt "0.8.2"]
            [io.taylorwood/lein-native-image "0.3.1"]]
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.eclipse.rdf4j/rdf4j-rio-turtle "4.0.0"]
                 [deercreeklabs/lancaster "0.9.20"]
                 [ont-app/vocabulary "0.1.7"]
                 [cli-matic "0.5.3"]
                 [org.clojars.quoll/asami "2.3.2"]
                 [com.github.seancorfield/honeysql "2.2.891"]]
  :native-image {:name "metamorph"
                 :opts ["--verbose" "--no-fallback"
                        "--initialize-at-build-time=com.fasterxml.jackson.core,com.fasterxml.jackson.dataformat"]}
  :main ^:skip-aot metamorph.core
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[lambdaisland/kaocha "1.69.1069"]
                                  [com.github.clj-easy/graal-build-time "0.1.4"]]}})
