; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(defproject metamorph "0.2.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :plugins [[lein-cljfmt "0.8.2"]]
  :main ^:skip-aot metamorph.core
  :profiles {:dev {:dependencies [[lambdaisland/kaocha "1.69.1069"]]}}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.eclipse.rdf4j/rdf4j-rio-turtle "4.0.0"]
                 [deercreeklabs/lancaster "0.9.17"]
                 [ont-app/vocabulary "0.1.7"]
                 [cli-matic "0.5.3"]
                 [org.clojars.quoll/asami "2.3.2"]
                 [com.github.seancorfield/honeysql "2.2.891"]]
  :aliases {"kaocha" ["run" "-m" "kaocha.runner"]})
