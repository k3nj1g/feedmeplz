(ns app.current-menu.controller
  (:require [clojure.string :as str]

            [re-frame.core :refer [reg-event-db reg-event-fx]]
            [tick.core :as t]
            [goog.string :as gstr]
            
            [app.current-menu.form :as form])

  (:require-macros [ps]))

(reg-event-fx
 :current-menu
 (fn [& _]
   {:http/request [{:method :get
                    :uri    "/categories"
                    :pid    ::categories}
                   {:method :get
                    :uri    "/daily-menus"
                    :params {:date (t/today)}
                    :pid    ::daily-menus}]
    :dispatch     [:zf/init form/form-path form/form-schema]}))

(reg-event-db
 ::add-to-cart
 (fn [db [_ item]]
   (update-in db [:page :cart (:id item)] (fnil inc 0))))

(reg-event-db
 ::remove-from-cart
 (fn [db [_ item]]
   (let [new-count (dec (get-in db [:page :cart (:id item)] 0))]
     (if (pos? new-count)
       (assoc-in db [:page :cart (:id item)] new-count)
       (update-in db [:page :cart] dissoc (:id item))))))

(defn get-order-summary-text 
  [cart-total items-in-cart]
  (let [item-lines (map #(gstr/format
                          "%s - %d шт. × %d ₽ = %d ₽"
                          (:name %)
                          (:quantity %)
                          (:price %)
                          (* (:quantity %) (:price %)))
                        items-in-cart)]
    (str (clojure.string/join "\n" item-lines)
         "\n\nИтого: " cart-total " ₽")))

(reg-event-fx
  ::copy-order-to-clipboard
  (fn [_ [_ cart-total items-in-cart]]
    (let [order-summary (get-order-summary-text cart-total items-in-cart)]
      {:fx [[:copy-to-clipboard order-summary]
            [:dispatch [::show-copied-notification]]]})))

(reg-event-fx
 ::show-copied-notification
 (fn [{:keys [db]} _]
   {:db             (assoc-in db [:page :copied] true)
    :dispatch-later [{:ms       2000
                      :dispatch [:db/set [:page :copied] false]}]}))
