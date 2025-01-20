(ns app.admin.catalog.controller
  (:require
   [re-frame.core :refer [reg-event-fx]]

   [app.admin.catalog.form :as form]))

(reg-event-fx
 :admin-catalog
 (fn [& _]
   {:http/request {:method  :get
                   :uri     "/categories"
                   :pid     ::categories
                   :success {:event ::set-active-category}}}))

(reg-event-fx
 ::set-active-category
 (fn [{db :db} [_ categories]]
   {:db (assoc-in db [:page :active-category] (first categories))}))

(reg-event-fx
 ::get-dishes-by-category
 (fn [_ [_ category_id]]
   {:http/request {:method :get
                   :uri    (str "/categories/" category_id "/dishes")
                   :pid    ::dishes-by-category}}))

(reg-event-fx
 ::save-dish-flow
 (fn [_ [_ active-category]]
   {:dispatch [:zf/eval-form form/form-path
               {:data {:category-id (:id active-category)}
                :success {:event ::save-dish}}]}))

(reg-event-fx
 ::save-dish
 (fn [_ [_ {:keys [data]}]]
   {:http/request {:method  :post
                   :uri     "/dishes"
                   :body    (assoc (:form-value data) :category_id (:category-id data))
                   :pid     ::save-dish
                   :success {:event ::dish-saved}}}))

(reg-event-fx
 ::dish-saved
 (fn [& _]
   {:toast {:message "Блюдо успешно добавлено"
            :type    :success}}))
