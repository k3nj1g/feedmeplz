(ns app.telegram.events
  (:require [re-frame.core :refer [reg-event-db reg-event-fx reg-fx]]
            
            [app.telegram.utils :as utils]))

;; Telegram events
(reg-event-db
 ::set-telegram-data
 (fn [db [_ data]]
   (assoc-in db [:telegram] data)))

(reg-event-fx
 ::send-telegram-order
 (fn [{:keys [db]} [_ order-data]]
   (let [telegram-data (get-in db [:telegram])]
     {:fx [[:dispatch [::telegram-send-data order-data]]
           [:dispatch [:show-toast {:message "Заказ отправлен!"
                                  :type :success}]]]})))

(reg-event-fx
 ::telegram-send-data
 (fn [_ [_ data]]
   {:fx [[:telegram-send-data data]]}))

(reg-fx
 :telegram-send-data
 (fn [data]
   (utils/send-data data)))

(reg-event-fx
 ::show-telegram-popup
 (fn [_ [_ title message]]
   {:fx [[:telegram-show-popup title message]]}))

(reg-fx
 :telegram-show-popup
 (fn [[title message]]
   (utils/show-popup title message)))

