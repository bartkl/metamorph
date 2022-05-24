(ns schema-transformer.rdf.datatype
  (:require [clojure.string :as string])
  (:import (org.eclipse.rdf4j.model.base CoreDatatype$XSD CoreDatatype$RDF)))

(defn rdf-list->seq [rdf-list]
  (if (nil? rdf-list) '() ;; TODO: Improve.
      (loop [l rdf-list
             s (list)]
        (if (= l :rdf/nil)
          s
          (recur (l :rdf/rest) (conj s (l :rdf/first)))))))

(defn literal->type [x]
  (condp #(= (.getCoreDatatype %2) %1) x
    CoreDatatype$XSD/STRING (.stringValue x)
    CoreDatatype$XSD/BOOLEAN (.booleanValue x)
    CoreDatatype$XSD/INTEGER (.intValue x)
    CoreDatatype$XSD/LONG (.integerValue x)
    CoreDatatype$XSD/DECIMAL (.decimalValue x)
    CoreDatatype$XSD/FLOAT (.floatValue x)
    CoreDatatype$XSD/DOUBLE (.doubleValue x)
    CoreDatatype$XSD/DURATION (.stringValue x)
    CoreDatatype$XSD/DATETIME (.stringValue x)
    CoreDatatype$XSD/TIME (.stringValue x)
    CoreDatatype$XSD/DATE (.stringValue x)
    CoreDatatype$XSD/ANYURI (.stringValue x)
    CoreDatatype$RDF/LANGSTRING (str x)))
