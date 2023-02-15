; SPDX-FileCopyrightText: 2022 - 2023 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(defproject metamorph "0.3.1"
  :description "Metamorph is a Clojure libary that allows the generation of a variety of schemas from a DX-PROF profile."
  :url "https://github.com/alliander-opensource/metamorph"
  :license {:name "Apache License 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :plugins [[lein-cljfmt "0.8.2"]
            [io.taylorwood/lein-native-image "0.3.1"]
            [lein-codox "0.10.8"]]
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.eclipse.rdf4j/rdf4j-rio-turtle "4.0.0"]
                 [deercreeklabs/lancaster "0.9.20"]
                 [ont-app/vocabulary "0.2.0"]
                 [cli-matic "0.5.3"]
                 [codox-theme-rdash "0.1.2"]
                 [tick "0.5.0"]
                 [org.slf4j/slf4j-nop "2.0.2"]
                 [org.clojars.quoll/asami "2.3.2"]
                 [com.github.seancorfield/honeysql "2.2.891"]]
  :aliases {"kaocha" ["run" "-m" "kaocha.runner"]}
  :native-image {:name "metamorph"
                 :opts ["--verbose" "--no-fallback" "-H:ReflectionConfigurationFiles=META-INF/native-image/reflect-config.json"
                        "--initialize-at-build-time=com.fasterxml.jackson.core,com.fasterxml.jackson.dataformat"]}
  :main ^:skip-aot metamorph.core
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[lambdaisland/kaocha "1.69.1069"]
                                  [com.github.clj-easy/graal-build-time "0.1.4"]]}}
  :cljfmt {:remove-consecutive-blank-lines? false  ;; Tonsky formatting.
           :indents ^:replace {#"^[-*+!?_a-zA-Z]" [[:inner 0]]}}
  :codox {:metadata {:doc/format :markdown}
        :themes [:rdash]})
