(ns app.current-menu.controller
  (:require [clojure.string :as str]

            [re-frame.core :refer [reg-event-db reg-event-fx]]
            [tick.core :as t]
            
            [goog.string :as gstr]
            [goog.string.format]
            
            [app.current-menu.form :as form]))

(reg-event-fx
 :current-menu
 (fn [& _]
   {:http/request [{:method :get
                    :uri    "/api/public/categories"
                    :pid    ::categories}
                   {:method :get
                    :uri    "/api/public/daily-menus"
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
       (-> db
           (update-in [:page :cart] dissoc (:id item))
           (update-in [:page :item-containers] dissoc (:id item)))))))

(reg-event-db
 ::set-item-container
 (fn [db [_ item-id container-number]]
   (assoc-in db [:page :item-containers item-id] container-number)))

(reg-event-db
 ::toggle-container-mode
 (fn [db _]
   (update-in db [:page :containers-mode] not)))

(reg-event-db
 ::add-container
 (fn [db _]
   (update-in db [:page :containers-count] (fnil inc 2))))

(defn get-order-summary-text 
  [cart-total items-in-cart item-containers]
  (let [items-by-container (->> items-in-cart
                                (group-by #(get item-containers (:id %) 1))
                                (sort-by key))
        container-lines (map (fn [[container items]]
                               (str "Контейнер " container ":\n"
                                    (->> items
                                         (map #(gstr/format
                                                "%s - %d шт. × %d ₽ = %d ₽"
                                                (:name %)
                                                (:quantity %)
                                                (:price %)
                                                (* (:quantity %) (:price %))))
                                         (str/join "\n"))))
                             items-by-container)]
    (str (str/join "\n\n" container-lines)
         "\n\nИтого: " cart-total " ₽")))

(reg-event-fx
  ::copy-order-to-clipboard
  (fn [_ [_ cart-total items-in-cart item-containers]]
    (let [order-summary (get-order-summary-text cart-total items-in-cart item-containers)]
      {:fx [[:copy-to-clipboard order-summary]
            [:dispatch [::show-copied-notification]]]})))

(reg-event-fx
 ::show-copied-notification
 (fn [{:keys [db]} _]
   {:db             (assoc-in db [:page :copied] true)
    :dispatch-later [{:ms       2000
                      :dispatch [:db/set [:page :copied] false]}]}))
