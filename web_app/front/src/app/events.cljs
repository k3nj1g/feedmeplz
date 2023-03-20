(ns app.events
  (:require [re-frame.core :refer [reg-event-fx reg-event-db]]
            [day8.re-frame.tracing :refer-macros [fn-traced]]

            [app.db :as db]))

(reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(reg-event-fx
 :navigate
 (fn-traced [_ [_ handler]]
   {:navigate handler}))

(reg-event-fx
 ::set-active-page
 (fn-traced [{:keys [db]} [_ {:keys [page]}]]
   {:db       (assoc db :active-page page)
    :dispatch [page]}))
