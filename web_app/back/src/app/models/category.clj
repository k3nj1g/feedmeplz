(ns app.models.category
  (:require [app.models.abstract-model :refer [->AbstractModel]]))

(def Schema
  [:map
   [:name [:string {:min 1, :max 50}]]
   [:description {:optional true} [:string]]])

(defn model
  [datasource]
  (->AbstractModel :categories Schema datasource))
