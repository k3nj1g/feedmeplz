(ns app.admin.catalog.controller
  (:require
   [re-frame.core :refer [reg-event-fx]]

   [app.admin.catalog.form :as form]))

(reg-event-fx
 :admin-catalog
 (fn [& _]
   {:http/request {:method  :get
                   :uri     "/api/public/categories"
                   :pid     ::categories
                   :success {:event ::init-active-category}}}))

(reg-event-fx
 ::init-active-category
 (fn [_ [_ categories]]
   (let [category (first categories)]
     {:dispatch [::set-active-category category]})))

(reg-event-fx
 ::set-active-category
 (fn [{db :db} [_ category]]
   {:db (assoc-in db [:page :active-category] category)
    :fx [[:dispatch [:zf/init form/form-path-search form/form-schema-search {}]]
         [:dispatch [::get-dishes-by-category (:id category)]]]}))

(reg-event-fx
 ::get-dishes-by-category
 (fn [_ [_ category_id]]
   {:http/request {:method :get
                   :uri    (str "/api/public/categories/" category_id "/dishes")
                   :pid    ::dishes-by-category}}))

(reg-event-fx
 ::save-dish-flow
 (fn [_ [_ active-category dish]]
   {:dispatch [:zf/eval-form form/form-path-update
               {:data    {:category-id (:id active-category)
                          :dish        dish}
                :success {:event (if dish ::update-dish ::create-dish)}}]}))

(reg-event-fx
 ::create-dish
 (fn [_ [_ {:keys [data]}]]
   {:http/request {:method  :post
                   :uri     "/api/dishes"
                   :body    (assoc (:form-value data) :category_id (:category-id data))
                   :success {:event  ::save-success
                             :params data}}}))

(reg-event-fx
 ::update-dish
 (fn [_ [_ {:keys [data]}]]
   {:http/request {:method  :put
                   :uri     (str "/api/dishes/" (:id (:dish data)))
                   :body    (:form-value data)
                   :success {:event  ::save-success
                             :params data}}}))

(reg-event-fx
 ::save-success
 (fn [_ [_ {:keys [category-id dish]}]]
   {:toast {:message (if dish "Блюдо успешно обновлено" "Блюдо успешно сохранено")
            :type    :success}
    :fx    [[:dispatch [:close-dialog :edit-dish]]
            [:dispatch [::get-dishes-by-category category-id]]]}))

(reg-event-fx
 ::init-edit-dish
 (fn [_ [_ dish]]
   {:fx [[:dispatch [:open-dialog :edit-dish dish]]
         [:dispatch [:zf/init form/form-path-update form/form-schema-update dish]]]}))

(reg-event-fx
 ::delete-dish
 (fn [_ [_ dish]]
   {:http/request {:method  :delete
                   :uri     (str "/api/dishes/" (:id dish))
                   :success {:event ::delete-success}}}))

(reg-event-fx
 ::delete-success
 (fn [_ [_ dish]]
   {:toast {:message "Блюдо удалено"
            :type    :warning}
    :fx    [[:dispatch [:close-dialog :delete-dish]]
            [:dispatch [::get-dishes-by-category (:category_id dish)]]]}))
