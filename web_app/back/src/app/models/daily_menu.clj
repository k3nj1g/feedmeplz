(ns app.models.daily-menu
  (:require [app.models.abstract-model :refer [->AbstractModel]]))

(def Schema
  [:map
   [:id {:optional true} :int]
   [:date :inst]
   [:dish_id :int]
   [:created_at {:optional true} :inst]
   [:updated_at {:optional true} :inst]])

(defn model
  [datasource]
  (->AbstractModel :daily_menu Schema datasource))
