(ns app.admin.catalog.model
  (:require [re-frame.core :refer [reg-sub]]
            
            [app.helpers :as h]
                        
            [app.admin.catalog.controller :as ctrl]))

(reg-sub
 ::data
 (fn [db]
   {:active-category (-> db :page :active-category)}))

(reg-sub
 ::categories
 (fn [db]
   (->> db :http/response ::ctrl/categories)))

(reg-sub
 ::dishes-by-category
 (fn [db _]
   (->> db :http/response ::ctrl/dishes-by-category)))

(reg-sub
 ::edit-dish-dialog-data
 :<- [::data]
 (fn [{:keys [active-category]} _]
   {:on-save (h/action [::ctrl/save-dish-flow active-category])}))
