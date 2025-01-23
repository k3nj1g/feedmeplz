(ns app.admin.daily.model
  (:require [re-frame.core :refer [reg-sub]]
            
            [app.admin.daily.controller :as ctrl]))

(reg-sub
 ::categories
 (fn [db]
   (->> db :http/response ::ctrl/categories)))

(reg-sub
 ::dishes-by-category
 :<- [::categories]
 (fn [db [_ category-id]]
   (->> db :http/response ::ctrl/categories
        (filter #(= category-id (:category_id %))))))
