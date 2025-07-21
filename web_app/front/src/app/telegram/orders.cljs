(ns app.telegram.orders
  (:require [re-frame.core :as rf]
            [app.telegram.core :as telegram]
            [clojure.string :as str]))

(defn format-order-message [items total]
  (let [item-lines (map #(str (:name %) " - " (:quantity %) " шт. × " (:price %) " ₽ = " (* (:quantity %) (:price %)) " ₽")
                        items)
        message (str "Новый заказ:\n\n"
                    (str/join "\n" item-lines)
                    "\n\nИтого: " total " ₽")]
    message))

(defn send-order [items total]
  (let [message (format-order-message items total)
        user (telegram/get-telegram-user)
        order-data {:type :order
                   :user user
                   :items items
                   :total total
                   :message message}]
    (telegram/send-data order-data)
    (telegram/show-popup "Заказ отправлен" "Ваш заказ успешно отправлен!")))

(rf/reg-event-fx
 ::send-telegram-order
 (fn [_ [_ items]]
   (let [total (reduce + (map #(* (:price %) (:quantity %)) items))]
     {:fx [[:dispatch [::send-order items total]]]})))

(rf/reg-event-fx
 ::send-order
 (fn [_ [_ items total]]
   (send-order items total)
   {})) 