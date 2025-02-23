(ns app.auth.events
  (:require [clojure.string :as str]
            
            [ajax.core     :as ajax]
            [re-frame.core :refer [inject-cofx reg-event-fx reg-fx]]
            
            [goog.crypt.base64 :as b64]))

(def auth-token-key "auth-token")

(reg-event-fx
 ::set-token
 (fn [{:keys [db]} [_ token]]
   {:db       (-> db
                  (assoc-in [:auth :token] token)
                  (assoc-in [:auth :authenticated?] true))
    :dispatch [::store-token token]}))

(reg-event-fx
 ::store-token
 (fn [_ [_ token]]
   {::store-token token}))

(reg-event-fx
 ::remove-token
 (fn [_ _]
   {::remove-token {}}))

(defn- parse-jwt
  [token]
  (let [[_ payload _] (str/split token #"\.")]
    (js->clj (.parse js/JSON (b64/decodeString payload)) :keywordize-keys true)))

(defn token-expired?
  [token]
  (let [{:keys [exp]} (parse-jwt token)
        current-time  (/ (.getTime (js/Date.)) 1000)]
    (< exp current-time)))

(reg-event-fx
 ::init
 [(inject-cofx :local-store auth-token-key)]
 (fn [{:keys [db local-store]} _]
   (let [token (get local-store auth-token-key)]
     (when (and token (not (token-expired? token)))
       {:db       (-> db
                      (assoc-in [:auth :token] token)
                      (assoc-in [:auth :authenticated?] true))
        :dispatch [::get-user-info]}))))

(reg-event-fx
 ::get-user-info
 (fn [& _]
   {:http/request {:method  :get
                   :uri     "/api/users/self/"
                   :success {:event ::set-user-info}
                   :failure {:event ::handle-auth-error}}}))

(reg-event-fx
 ::handle-auth-error
 (fn [_ [_ response]]
   (if (= (:status response) 401)
     {:dispatch [::logout]}
     {:toast {:message "Не удалось авторизоваться. Проверьте введенные данные."
              :type    :error} })))

(reg-event-fx
 ::set-user-info
 (fn [{:keys [db]} [_ user-info]]
   {:db (assoc-in db [:auth :user-info] user-info)}))

(reg-event-fx
 ::logout
 (fn [{:keys [db]} _]
   {:db (-> db
            (assoc-in [:auth :authenticated?] false)
            (assoc-in [:auth :token] nil)
            (assoc-in [:auth :user-info] nil))
    :fx [[:dispatch [::remove-token]]
         [:navigate [:login]]]}))

(reg-fx
 ::store-token
 (fn [token]
   (.setItem js/localStorage auth-token-key token)))

(reg-fx
 ::remove-token
 (fn []
   (.removeItem js/localStorage auth-token-key)))

(reg-event-fx
 ::refresh-token
 (fn [{:keys [db]} [_ token original-request]]
   {:http-xhrio {:method           :post
                 :uri              (str (get-in db [:config :api-url]) "/api/public/auth/refresh-token")
                 :timeout          8000
                 :format           (ajax/json-request-format)
                 :response-format  (ajax/json-response-format {:keywords? true})
                 :on-success       [::refresh-token-success original-request]
                 :on-failure       [::refresh-token-failure]
                 :headers          {"Authorization" (str "Bearer " token)}
                 :with-credentials true}}))

(reg-event-fx
 ::refresh-token-success
 (fn [{:keys [db]} [_ original-request response]]
   {:db         (assoc-in db [:auth :token] (:token response))
    :http-xhrio original-request}))

(reg-event-fx
 ::refresh-token-failure
 (fn [_ _]
   {:dispatch [::logout]}))
