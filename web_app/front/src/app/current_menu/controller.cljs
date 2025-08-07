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

(reg-event-db
 ::create-container
 (fn [db [_ container-id]]
   (assoc-in db [:page :containers container-id] [])))

(reg-event-db
 ::add-item-to-container
 (fn [db [_ container-id item-id quantity]]
   (let [menu-items (-> db :http/response :app.current-menu.controller/daily-menus :data first :menu-items)
         item (first (filter #(= (:id %) item-id) menu-items))
         containers-path [:page :containers container-id]
         current-items (vec (get-in db containers-path []))
         idx (first (keep-indexed (fn [i entry] (when (= (:id (:item entry)) item-id) i)) current-items))]
     (cond
       (nil? item) db ; если item не найден, ничего не делаем
       (some? idx)
       (update-in db (conj containers-path idx :quantity) #(+ % quantity))
       :else
       (update-in db containers-path (fnil conj []) {:item item :quantity quantity})))))

(reg-event-db
 ::move-item-between-containers
 (fn [db [_ source-container-id target-container-id item-id quantity]]
   (let [source-path [:page :containers source-container-id]
         target-path [:page :containers target-container-id]
         source-items (vec (get-in db source-path []))
         idx (first (keep-indexed (fn [i entry] (when (= (:id (:item entry)) item-id) i)) source-items))
         moving (when (some? idx)
                  (assoc (nth source-items idx) :quantity quantity))
         rest-items (if (some? idx)
                      (vec (concat (subvec source-items 0 idx) (subvec source-items (inc idx))))
                      source-items)]
     (cond-> db
       (some? idx) (assoc-in source-path rest-items)
       moving      (update-in target-path (fnil conj []) moving)))))

(reg-event-db
 ::add-new-container
 (fn [db _]
   (let [containers (get-in db [:page :containers] {})
         new-id (if (seq containers)
                  (inc (apply max (keys containers)))
                  1)]
     (assoc-in db [:page :containers new-id] []))))
