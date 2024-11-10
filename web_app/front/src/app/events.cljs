(ns app.events
  (:require [ajax.core     :as ajax]
            [re-frame.core :refer [dispatch reg-event-fx reg-event-db reg-fx]]
            [day8.re-frame.http-fx]

            [app.db :as db]))

(reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(reg-event-fx
 :navigate
 (fn [_ [_ handler]]
   {:navigate handler}))

(reg-event-fx
 ::set-active-page
 (fn [{:keys [db]} [_ {:keys [page]}]]
   {:db       (assoc db :active-page page)
    :dispatch [page]}))

(reg-event-db
 :put-response
 (fn [db [_ params resp]]
   (when-let [pid (:pid params)]
     (assoc-in db [:http/response pid] resp))))

(reg-event-fx
 :http/request
 (fn [{:keys [db]} [_ params]]
   {:http-xhrio (-> params
                    (update :uri (partial str (get-in db [:config :api-url])))
                    (merge {:timeout         8000
                            :response-format (ajax/json-response-format {:keywords? true})
                            :on-success      [:put-response params]
                            :on-failure      [:bad-http-result]}))}))

(reg-fx
 :http/request
 (fn [params]
   (dispatch [:http/request params])))
