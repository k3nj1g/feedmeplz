(ns app.admin.daily.model
  (:require [clojure.string :as str]
            
            [re-frame.core :refer [reg-sub subscribe]]

            [app.helpers :as h]
            
            [app.admin.daily.controller :as ctrl]
            [app.admin.daily.form       :as form]))

(defn- match-all-terms?
  [name search-terms]
  (every? #(str/includes? (str/lower-case name) %) search-terms))

(reg-sub
 ::categories
 :<- [:http/response ::ctrl/categories]
 (fn [categories _]
   categories))

(reg-sub
 ::dishes-by-category
 (fn [[_ category] _]
   [(subscribe [:http/response ::ctrl/dishes])
    (subscribe [:zf/get-value form/form-path [(form/category->path category) :search]])])
 (fn [[dishes search] [_ {category-id :id}]]
   (let [search-terms (some-> search
                              (str/lower-case)
                              (str/split #"\s+"))]
     (cond->> (filter #(= category-id (:category_id %)) dishes)
       search
       (filter #(match-all-terms? (:name %) search-terms))))))

(reg-sub
 ::selected-dishes-by-category
 (fn [[_ category] _]
   [(subscribe [:zf/get-value form/form-path [(form/category->path category) :dishes]])])
 (fn [[selected-items] _]
   (sort-by :name selected-items)))

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
 (fn [selected-items-count _]
   {:save {:on-click (h/action [::ctrl/create-daily-menu-flow])
           :disabled (= selected-items-count 0)}}))
