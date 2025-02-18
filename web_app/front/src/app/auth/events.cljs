(ns app.auth.events
  (:require [re-frame.core :refer [inject-cofx reg-event-fx reg-fx]]))

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
 ::load-token
 [(inject-cofx :local-store auth-token-key)]
 (fn [{:keys [db local-store]} _]
   (let [token (get local-store auth-token-key)]
     {:db (cond-> (assoc-in db [:auth :token] token)
            token
            (assoc-in [:auth :authenticated?] true))})))

(reg-event-fx
 ::logout
 (fn [{:keys [db]} _]
   {:db (-> db
            (assoc-in [:auth :authenticated?] false)
            (assoc-in [:auth :token] nil))
    :dispatch-n [[::remove-token]
                 [:navigate :login]]}))

(reg-fx
 ::store-token
 (fn [token]
   (.setItem js/localStorage auth-token-key token)))

(reg-fx
 ::remove-token
 (fn []
   (.removeItem js/localStorage auth-token-key)))
