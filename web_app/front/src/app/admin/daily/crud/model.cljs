(ns app.admin.daily.crud.model
  (:require [re-frame.core :refer [reg-sub subscribe]]

            [app.helpers :as h]

            [app.admin.daily.crud.controller :as ctrl]
            [app.admin.daily.crud.form       :as form]))

(reg-sub
 ::categories
 :<- [:http/response ::ctrl/categories]
 (fn [categories _]
   categories))

(reg-sub
 ::daily-menu-date
 :<- [:http/response ::ctrl/daily-menu]
 (fn [daily-menu _]
   (:date daily-menu)))

(reg-sub
 ::dishes-by-category
 (fn [[_ category] _]
   [(subscribe [:http/response ::ctrl/dishes])
    (subscribe [:zf/get-value form/form-path [(form/category->path category) :search]])])
 (fn [[dishes search] [_ {category-id :id}]]
   (cond->> (filter #(= category-id (:category_id %)) dishes)
     search
     (filter #(h/match-search-term? (:name %) search)))))

(reg-sub
 ::selected-dishes-by-category
 (fn [[_ category] _]
   [(subscribe [:zf/get-value form/form-path [(form/category->path category) :dishes]])])
 (fn [[selected-items] _]
   (->> selected-items
        (sort-by :name)
        (map #(assoc % :on-edit (h/action [::ctrl/init-edit-dish %]))))))

(reg-sub
 ::selected-items-count
 :<- [:zf/get-value form/form-path]
 (fn [form-value _]
   (->> form-value
        (vals)
        (mapcat :dishes)
        (count))))

(reg-sub
 ::buttons
 :<- [::selected-items-count]
 :<- [:db/get [:route-params :id]] 
 (fn [[selected-items-count id] _]
   {:save {:on-click (h/action (if id [::ctrl/update-daily-menu-flow] [::ctrl/create-daily-menu-flow]))
           :disabled (= selected-items-count 0)}}))

(reg-sub
 ::edit-dish-dialog-data
 (fn [db _]
   (let [dish (get-in db [:dialogs :edit-dish :data])]
     {:on-save (h/action [::ctrl/save-dish-flow dish])})))
