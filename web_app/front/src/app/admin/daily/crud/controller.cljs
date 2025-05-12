(ns app.admin.daily.crud.controller
  (:require [re-frame.core :refer [reg-event-fx]]

            [tick.core     :as t]
            [zenform.model :as zf]

            [app.helpers :as h]

            [app.utils.date :as date-utils]

            [app.admin.daily.crud.form :as form]))

;;--- Init events ---
(reg-event-fx
 :admin-daily-create
 (fn [& _]
   {:http/request [{:method :get
                    :uri    "/api/public/categories"
                    :pid    ::categories
                    :success {:event ::init-blank-form}}
                   {:method :get
                    :uri    "/api/public/dishes"
                    :pid    ::dishes}]}))

(reg-event-fx
 ::init-blank-form
 (fn [_ [_ categories]]
   {:dispatch [:zf/init form/form-path (form/form-schema categories) {:date (date-utils/to-iso-date (js/Date.))}]}))

(reg-event-fx
 :admin-daily-update
 (fn [_ [_ params]]
   {:http/request [{:method  :get
                    :uri     "/api/public/categories"
                    :pid     ::categories
                    :success {:event ::init-form}}
                   {:method  :get
                    :uri     "/api/public/dishes"
                    :pid     ::dishes
                    :success {:event ::init-form}}
                   {:method  :get
                    :uri     (str "/api/public/daily-menus/" (:id params))
                    :pid     ::daily-menu
                    :success {:event ::init-form}}]}))

(reg-event-fx
 ::init-form
 (fn [{db :db} & _]
   (let [categories (get-in db [:http/response ::categories])
         dishes     (get-in db [:http/response ::dishes])
         daily-menu (get-in db [:http/response ::daily-menu])]
     (when (and categories dishes daily-menu)
       (let [init-data (->> daily-menu :menu_items
                            (group-by :category_id)
                            (reduce-kv
                             (fn [acc k v]
                               (assoc acc (form/category->path {:id k})
                                      {:dishes (mapv
                                                (fn [{dish-id :dish_id id :id}]
                                                  (some #(when (= (:id %) dish-id)
                                                           (assoc % :item-id id)) dishes)) v)})) {}))]
         {:dispatch [:zf/init form/form-path (form/form-schema categories)
                     (merge-with merge {:date (:date daily-menu)} init-data)]})))))
;;------

;;--- Flow events ---
(reg-event-fx
 ::create-daily-menu-flow
 (fn []
   {:dispatch [:zf/eval-form form/form-path
               {:success
                {:event  ::check-menu-date
                 :params {:success
                          {:event  ::check-existing-menu
                           :params {:success
                                    {:event  ::handle-existing-menu
                                     :params {:success
                                              {:event  ::create-daily-menu
                                               :params {:success {:event ::save-success}}}}}}}}}}]}))

(reg-event-fx
 ::update-daily-menu-flow
 (fn []
   {:dispatch [:zf/eval-form form/form-path
               {:success
                {:event  ::check-menu-date
                 :params {:success
                          {:event  ::update-daily-menu
                           :params {:success {:event ::save-success}}}}}}]}))

(reg-event-fx
 ::check-menu-date
 (fn [_ [_ {:keys [success data]}]]
   (let [selected-date (get-in data [:form-value :date])]
     (if (t/< (t/date selected-date) (t/date))
       {:toast {:message "Запрещено создание/изменение меню для прошлых дат"
                :type    :error}}
       {:dispatch (h/success-event-to-dispatch success data)}))))


(reg-event-fx
 ::check-existing-menu
 (fn [_ [_ {:keys [success data]}]]
   (let [selected-date (get-in data [:form-value :date])]
     {:http/request {:method  :get
                     :uri     "/api/public/daily-menus"
                     :params  {:date selected-date}
                     :success (h/success-event success data)}})))

(reg-event-fx
 ::handle-existing-menu
 (fn [_ [_  {:keys [success data]} existing-menu]]
   (if (seq existing-menu)
     {:toast {:message "Меню на эту дату уже существует"
              :type    :error}}
     {:dispatch (h/success-event-to-dispatch success data)})))

(reg-event-fx
 ::create-daily-menu
 (fn [_ [_ {:keys [success data]}]]
   {:http/request {:method  :post
                   :uri     "/api/daily-menus"
                   :body    {:date   (get-in data [:form-value :date])
                             :dishes (->> data :form-value
                                          (vals)
                                          (mapcat (comp :dishes))
                                          (remove nil?))}
                   :success (h/success-event success data)}}))

(reg-event-fx
 ::update-daily-menu
 (fn [{db :db} [_ {:keys [success data]}]]
   {:http/request {:method  :put
                   :uri     (str "/api/daily-menus/" (get-in db [:route-params :id]))
                   :body    {:date   (get-in data [:form-value :date])
                             :dishes (->> data :form-value
                                          (vals)
                                          (mapcat (comp :dishes))
                                          (sort-by (juxt :category_id :name))
                                          (remove nil?))}
                   :success (h/success-event success data)}}))

(reg-event-fx
 ::save-success
 (fn [& _]
   {:toast    {:message "Меню успешно сохранено"
               :type    :success}
    :dispatch [:navigate :admin-daily-list]}))
;;------

;;--- Edit dish events ---
(reg-event-fx
 ::init-edit-dish
 (fn [_ [_ dish]]
   {:fx [[:dispatch [:open-dialog :edit-dish dish]]
         [:dispatch [:zf/init form/form-path-update form/form-schema-update dish]]]}))

(reg-event-fx
 ::save-dish-flow
 (fn [_ [_ dish]]
   {:dispatch [:zf/eval-form form/form-path-update
               {:data    {:dish dish}
                :success {:event ::update-dish}}]}))

(reg-event-fx
 ::update-dish
 (fn [_ [_ {:keys [data]}]]
   {:http/request {:method  :put
                   :uri     (str "/api/dishes/" (:id (:dish data)))
                   :body    (:form-value data)
                   :success {:event  ::dish-save-success
                             :params data}}}))

(reg-event-fx
 ::dish-save-success
 (fn [{db :db} [_ {:keys [form-value dish]}]]
   (let [category-dishes (zf/get-value (get-in db form/form-path) [(form/category->path {:id (:category_id dish)}) :dishes])]
     {:toast {:message "Блюдо успешно обновлено"
              :type    :success}
      :fx    [[:dispatch [:close-dialog :edit-dish]]
              [:dispatch [:zf/set-value
                          form/form-path
                          [(form/category->path {:id (:category_id dish)}) :dishes]
                          (mapv (fn [item] (if (= (:id item) (:id dish)) (merge item form-value) item)) category-dishes)]]]})))
