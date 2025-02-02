(ns app.admin.daily.crud.controller
  (:require [re-frame.core :refer [reg-event-fx]]

            [tick.core :as t]

            [app.helpers :as h]
            
            [app.admin.daily.crud.form :as form]))

(reg-event-fx
 :admin-daily-crud
 (fn [& _]
   {:http/request [{:method :get
                    :uri    "/categories"
                    :pid    ::categories
                    :success {:event ::init-form}}
                   {:method :get
                    :uri    "/dishes"
                    :pid    ::dishes}]}))

(reg-event-fx
 ::init-form
 (fn [_ [_ categories]]
   {:dispatch [:zf/init form/form-path (form/form-schema categories) {:date (t/date)}]}))

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
                     :uri     "/daily-menu"
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
                   :uri     "/daily-menu"
                   :body    (->> data :form-value
                                 vals
                                 (mapcat (comp :dishes))
                                 (hash-map :dishes))
                   :success (h/success-event success data)}}))

(reg-event-fx
 ::save-success
 (fn [& _]
   {:toast    {:message "Меню успешно сохранено"
               :type    :success}
    :dispatch [:navigate :admin-daily-list]}))
