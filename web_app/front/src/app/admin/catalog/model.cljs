(ns app.admin.catalog.model
  (:require [re-frame.core :refer [dispatch reg-sub]]
            
            [app.helpers :as h]
            
            [app.admin.catalog.controller :as ctrl])
  (:require-macros [ps]))

(reg-sub
 ::data
 (fn [db]
   {:active-category (-> db :page :active-category)}))

(reg-sub
 ::categories
 :<- [:http/response ::ctrl/categories]
 (fn [categories _]
   categories))

(defn change-category
  [category]
  (dispatch [::ctrl/set-active-category category]))

(reg-sub
 ::dishes-by-category
 (fn [db _]
   (->> db :http/response ::ctrl/dishes-by-category
        (map #(assoc %
                     :on-edit (h/action [::ctrl/init-edit-dish %])
                     :on-delete (h/action [:open-dialog :delete-dish %]))))))

(reg-sub
 ::edit-dish-dialog-data
 (fn [db _]
   (let [active-category (-> db :page :active-category)
         dish            (get-in db [:dialogs :edit-dish :data])]
     {:dish    dish
      :on-save (h/action [::ctrl/save-dish-flow active-category dish])})))

(reg-sub
 ::delete-dish-dialog-data
 (fn [db _]
   (let [dish (get-in db [:dialogs :delete-dish :data])]
     {:dish      dish
      :on-delete (h/action [::ctrl/delete-dish dish])})))
