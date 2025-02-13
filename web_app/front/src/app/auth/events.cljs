(ns app.auth.events
  (:require [re-frame.core :refer [inject-cofx reg-event-db reg-event-fx reg-fx]]))

(def auth-token-key "auth-token")

(reg-event-db
 ::set-authenticated
 (fn [db [_ authenticated?]]
   (assoc-in db [:auth :authenticated?] authenticated?)))

(reg-event-fx
 ::set-token
 (fn [{:keys [db]} [_ token]]
   {:db       (assoc-in db [:auth :token] token)
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
     (cond-> {:db (assoc-in db [:auth :token] token)}
       token
       (assoc :dispatch [::set-authenticated true])))))

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

(reg-event-fx
 ::check-auth
 (fn [_ _]
   {:dispatch [::load-token]}))
