(defproject schema-transformer "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :main ^:skip-aot schema-transformer.core
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.eclipse.rdf4j/rdf4j-rio-turtle "4.0.0"]
                 [deercreeklabs/lancaster "0.9.17"]
                 [ont-app/vocabulary "0.1.7"]
                 [cli-matic "0.5.3"]
                 [org.clojars.quoll/asami "2.3.1-SNAPSHOT"]])
