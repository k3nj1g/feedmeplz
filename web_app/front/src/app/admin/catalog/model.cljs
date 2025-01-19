(ns app.admin.catalog.model
  (:require [re-frame.core :refer [reg-sub]]
            
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
