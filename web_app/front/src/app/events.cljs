(ns app.events
  (:require [clojure.string :as str]
            
            [ajax.core     :as ajax]
            [re-frame.core :refer [dispatch reg-cofx reg-event-fx reg-event-db reg-fx]]
            [day8.re-frame.http-fx]

            [app.db :as db]))

(reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(reg-event-fx
 :navigate
 (fn [{db :db} [_ handler params]]
   (cond-> {:navigate [handler params]}
     (not= (:active-page db) handler)
     (assoc :db (dissoc db :page :form :http/response :route-params)))))

(reg-event-fx
 ::set-active-page
 (fn [{:keys [db]} [_ {:keys [page route-params]}]]
   (if (-> db :auth :authenticated?)
     (if (= page :login)
       {:navigate [:current-menu]}
       {:db       (assoc db :active-page page)
        :dispatch [page route-params]})
     (if (str/includes? (name page) "admin")
       {:navigate [:login]}
       {:db       (assoc db :active-page page)
        :dispatch [page route-params]}))))

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
 :open-dialog
 (fn [db [_ dialog-id data]]
   (assoc-in db [:dialogs dialog-id] {:open true :data data})))

(reg-event-db
 :close-dialog
 (fn [db [_ dialog-id]]
   (update-in db [:dialogs dialog-id] assoc :open false dissoc :data)))

(reg-event-db
 :update-dialog-data
 (fn [db [_ dialog-id data]]
   (update-in db [:dialogs dialog-id :data] merge data)))


(reg-event-db
 :put-response
 (fn [db [_ params resp]]
   (when-let [pid (:pid params)]
     (assoc-in db [:http/response pid] resp))))

(defn make-xhrio-request
  [db request]
  (let [token (get-in db [:auth :token])]
    (cond-> (-> request
                (update :uri (partial str (get-in db [:config :api-url])))
                (merge {:timeout         8000
                        :response-format (ajax/json-response-format {:keywords? true})
                        :on-success      [:success-http-result request]
                        :on-failure      [:bad-http-result]}))

      token
      (assoc-in [:headers "Authorization"] (str "Bearer " token))

      (#{:post :put :patch} (:method request))
      (-> (dissoc :body)
          (assoc :params (:body request)
                 :format (ajax/json-request-format)))

      (= :delete (:method request))
      (assoc :format (ajax/url-request-format)))))

(reg-event-fx
 :http/request
 (fn [{:keys [db]} [_ request]]
   {:http-xhrio (if (vector? request)
                  (mapv (partial make-xhrio-request db) request)
                  (make-xhrio-request db request))}))

(reg-event-fx
 :success-http-result
 (fn [_ [_ {:keys [success] :as params} resp]]
   {:fx (cond-> [[:dispatch [:put-response params resp]]]
          (:event success)
          (conj (->> [:dispatch (->> [(:event success) (:params success) resp]
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

(reg-fx
 :copy-to-clipboard
 (fn [text]
   (.writeText js/navigator.clipboard text)))

(reg-cofx
 :local-store
 (fn [coeffects local-store-key]
   (assoc-in coeffects [:local-store local-store-key] (js->clj (.getItem js/localStorage local-store-key)))))
