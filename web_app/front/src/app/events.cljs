(ns app.events
  (:require [ajax.core     :as ajax]
            [re-frame.core :refer [dispatch reg-event-fx reg-event-db reg-fx]]
            [day8.re-frame.http-fx]

            [zenform.model :as zf]

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
   {:http-xhrio (-> params
                    (update :uri (partial str (get-in db [:config :api-url])))
                    (merge {:timeout         8000
                            :response-format (ajax/json-response-format {:keywords? true})
                            :on-success      [:success-http-result params]
                            :on-failure      [:bad-http-result]}))}))

(reg-event-fx
 :success-http-result
 (fn [_ [_ {:keys [success] :as params} resp]]
   {:fx (cond-> [[:dispatch [:put-response params resp]]]
          (:event success)
          (conj (->> [:dispatch (->> [(:event success) (:params params) resp]
                                     (remove nil?)
                                     (vec))])))}))

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

(reg-event-fx
 :eval-form
 (fn [{db :db} [_ form-path {:keys [data success error]}]]
   (let [form-data                   (get-in db form-path)
         {:keys [errors value form]} (zf/eval-form form-data)]
     (cond-> {:db (assoc-in db form-path (assoc form :errors errors))
              :fx []}

       (and (empty? errors) success)
       (assoc :dispatch [(:event success) (assoc (:params success) :data
                                                 (merge data {:form-data  form
                                                              :form-value value}))])

       (seq errors)
       ((completing
         (.warn js/console "Form errors: " (clj->js errors))))

       (and (seq errors) error)
       (update :fx conj [:dispatch [(:event error) (assoc (:params error) :errors errors)]])))))
