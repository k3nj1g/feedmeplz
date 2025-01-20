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
 (fn [{db :db} [_ handler]]
   {:navigate handler
    :db       (dissoc db :page)}))

(reg-event-fx
 ::set-active-page
 (fn [{:keys [db]} [_ {:keys [page]}]]
   {:db       (assoc db :active-page page)
    :dispatch [page]}))

(reg-event-db
 :toggle-popup-menu
 (fn [db [_ popup-id]]
   (update db :popup-menu (fnil (fn [popup-menu] (if (contains? popup-menu popup-id)
                                                   (disj popup-menu popup-id)
                                                   (conj popup-menu popup-id))) #{}))))

(reg-event-db
 :close-popup-menu
 (fn [db [_ popup-id]]
   (update db :popup-menu (fnil (fn [popup-menu] (disj popup-menu popup-id)) #{}))))

(reg-event-db
 :toggle-dialog-menu
 (fn [db [_ dialog-id]]
   (update db :dialog-menu (fnil (fn [dialog-menu] (if (contains? dialog-menu dialog-id)
                                                     (disj dialog-menu dialog-id)
                                                     (conj dialog-menu dialog-id))) #{}))))

(reg-event-db
 :close-dialog-menu
 (fn [db [_ dialog-id]]
   (update db :dialog-menu (fnil (fn [dialog-menu] (disj dialog-menu dialog-id)) #{}))))


(reg-event-db
 :put-response
 (fn [db [_ params resp]]
   (when-let [pid (:pid params)]
     (assoc-in db [:http/response pid] resp))))

(reg-event-fx
 :http/request
 (fn [{:keys [db]} [_ params]]
   {:http-xhrio (cond-> (-> params
                            (update :uri (partial str (get-in db [:config :api-url])))
                            (merge {:timeout         8000
                                    :response-format (ajax/json-response-format {:keywords? true})
                                    :on-success      [:success-http-result params]
                                    :on-failure      [:bad-http-result]}))
                  (:body params)
                  (-> (dissoc :body)
                      (assoc :params          (:body params)
                             :format          (ajax/json-request-format)
                             :response-format (ajax/json-response-format {:keywords? true}))))}))

(reg-event-fx
 :success-http-result
 (fn [_ [_ {:keys [success] :as params} resp]]
   {:fx (cond-> [[:dispatch [:put-response params resp]]]
          (:event success)
          (conj (->> [:dispatch (->> [(:event success) (:params params) resp]
                                     (remove nil?)
                                     (vec))])))}))

(reg-event-fx
 :bad-http-result
 (fn [_ [_ resp]]
   {:toast {:message (str "Произошла ошибка: " (:status resp))
            :type    :error}}))

(reg-fx
 :http/request
 (fn [params]
   (dispatch [:http/request params])))


(reg-event-fx
 :db/set
 (fn [{:keys [db]} [_ path value]]
   {:db (if (vector? path)
          (assoc-in db path value)
          (assoc db path value))}))

