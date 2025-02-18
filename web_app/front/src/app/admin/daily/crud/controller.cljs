(ns app.admin.daily.crud.controller
  (:require [re-frame.core :refer [reg-event-fx]]

            [tick.core :as t]

            [app.helpers :as h]

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
   {:dispatch [:zf/init form/form-path (form/form-schema categories) {:date (t/date)}]}))

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
                    :uri     (str "/daily-menus/" (:id params))
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
                     (merge-with merge {:date (t/date (t/instant (:date daily-menu)))} init-data)]})))))
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
     (if (t/< selected-date (t/date))
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
                                          (remove nil?))}
                   :success (h/success-event success data)}}))

(reg-event-fx
 ::save-success
 (fn [& _]
   {:toast    {:message "Меню успешно сохранено"
               :type    :success}
    :dispatch [:navigate :admin-daily-list]}))
;;------
