(ns app.telegram.auth
  (:require [re-frame.core :as rf :refer [reg-event-db reg-event-fx reg-fx reg-sub]]))

(defn get-telegram-user
  []
  (let [webapp (.-WebApp js/window.Telegram)
        user   (.-initDataUnsafe webapp)]
    {:id         (.-id user)
     :first-name (.-first_name user)
     :last-name  (.-last_name user)
     :username   (.-username user)}))

(reg-event-db
 ::init-telegram-auth
 (fn [db _]
   (let [user (get-telegram-user)]
     (-> db
         (assoc-in [:telegram :auth :authenticated?] true)
         (assoc-in [:telegram :auth :user-info] user)))))

(reg-sub
 :telegram-auth
 (fn [db]
   (get-in db [:auth :telegram])))

(reg-sub
 :telegram-user
 (fn [db]
   (get-in db [:auth :telegram :user]))) 
