(ns app.models.dish
  (:require [app.models.abstract-model :refer [->AbstractModel]]))

(def Schema
  [:map
   [:name [:string {:min 1, :max 100}]]
   [:description {:optional true} [:string]]
   [:price [:double {:min 0}]]
   [:category_id [:int]]])

(defn model
  [datasource]
  (->AbstractModel :dishes Schema datasource))
