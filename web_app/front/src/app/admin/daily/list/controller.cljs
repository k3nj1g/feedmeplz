(ns app.admin.daily.list.controller
    (:require [re-frame.core :refer [reg-event-db reg-event-fx]]))

(def items-limit 10)

(reg-event-fx
 :admin-daily-list
 (fn [_ [_ {:keys [page] :or {page 1} :as params}]]
   {:fx [[:dispatch [::set-current-page page]]
         [:dispatch [::get-daily-menus params]]]}))

(reg-event-fx
 ::get-daily-menus
 (fn [_ [_ {:keys [page limit] :or {page 1 limit items-limit}}]]
   {:http/request [{:method :get
                    :uri    "/api/public/categories"
                    :pid    ::categories}
                   {:method :get
                    :uri     "/api/public/daily-menus"
                    :params  {:page page :limit limit}
                    :success {:event ::get-daily-menus-success}
                    :pid     ::daily-menus}]}))

(reg-event-db
 ::set-pagination
 (fn [db [_ pagination]]
   (assoc-in db [:page :pagination] pagination)))

(reg-event-db
 ::set-current-page
 (fn [db [_ page]]
   (assoc-in db [:page :current-page] page)))

(reg-event-db
 ::set-daily-menus
 (fn [db [_ daily-menus]]
   (assoc-in db [:page :daily-menus] daily-menus)))

(reg-event-fx
 ::get-daily-menus-success
 (fn [_ [_ response]]
   {:fx [[:dispatch [::set-daily-menus (:data response)]]
         [:dispatch [::set-pagination (:pagination response)]]]}))

(reg-event-fx
 ::change-page
 (fn [{db :db} [_ page]]
   (let [limit (get-in db [:page :pagination :limit] items-limit)]
     {:fx [[:dispatch [::get-daily-menus {:page page :limit limit}]]]})))
