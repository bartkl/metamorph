; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.malli.shacl
  (:require [malli.core :as m]
    [malli.registry :as mr]
    [malli.util :as mu]
    [malli.swagger :as swagger]
    [malli.error :as me]
    [malli.plantuml :as plantuml]
    [malli.dot :as dot]
    [metamorph.graph.db :refer [node-ref?]]))

(comment "Address example"
  (def Address
    [:schema
     {:registry {"Country" [:map
                            [:name [:enum :FI :PO]]
                            [:neighbors [:vector [:ref "Country"]]]]
                 "Burger" [:map
                           [:name string?]
                           [:description {:optional true} string?]
                           [:origin [:maybe "Country"]]
                           [:price pos-int?]]
                 "OrderLine" [:map
                              [:burger "Burger"]
                              [:amount int?]]
                 "Order" [:map
                          [:lines [:vector "OrderLine"]]
                          [:delivery [:map
                                      [:delivered boolean?]
                                      [:address [:map
                                                 [:street string?]
                                                 [:zip int?]
                                                 [:country "Country"]]]]]]}}
     "Order"])

  (-> (plantuml/transform Address)
    println)

  (-> (dot/transform Address)
    println)
  
  )

(comment "SHACL schema"
  (def NodeShape
    [:map 
     [:id string?]
     [:tags [:set keyword?]]
     [:address
      [:map
       [:street string?]
       [:city string?]
       [:zip int?]
       [:lonlat [:tuple double? double?]]]]])

  (-> (plantuml/transform NodeShape)
    println)

  (-> (dot/transform NodeShape)
    println)
  
  )

