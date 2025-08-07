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

(defn find-item-by-id [menu-items item-id]
  (first (filter #(= (:id %) item-id) menu-items)))

(reg-event-db
 ::add-item-to-container
 (fn [db [_ container-id item-id quantity]]
   (let [daily-menus-response (get-in db [:http/response ::daily-menus])
         menu-items (-> daily-menus-response :data first :menu-items)
         item (find-item-by-id menu-items item-id)]
     (if item
       (let [containers-path [:page :containers container-id]
             current-items (vec (get-in db containers-path []))
             existing-idx (first (keep-indexed
                                  (fn [i entry]
                                    (when (= (get-in entry [:item :id]) item-id) i))
                                  current-items))]
         (if existing-idx
           ;; Обновляем количество существующего элемента
           (update-in db (conj containers-path existing-idx :quantity) #(+ % quantity))
           ;; Добавляем новый элемент
           (update-in db containers-path (fnil conj []) {:item item :quantity quantity})))
       ;; Если item не найден, возвращаем db без изменений
       (do
         (js/console.warn "Item not found:" item-id)
         db)))))

(reg-event-db
 ::move-item-between-containers
 (fn [db [_ source-container-id target-container-id item-id quantity]]
   (let [source-path [:page :containers source-container-id]
         target-path [:page :containers target-container-id]
         source-items (vec (get-in db source-path []))
         item-idx (first (keep-indexed
                          (fn [i entry]
                            (when (= (get-in entry [:item :id]) item-id) i))
                          source-items))]
     (if item-idx
       (let [moving-item (nth source-items item-idx)
             updated-source-items (vec (concat (subvec source-items 0 item-idx)
                                               (subvec source-items (inc item-idx))))
             target-items (vec (get-in db target-path []))
             existing-target-idx (first (keep-indexed
                                         (fn [i entry]
                                           (when (= (get-in entry [:item :id]) item-id) i))
                                         target-items))]
         (cond-> db
           ;; Удаляем из источника
           true (assoc-in source-path updated-source-items)
           ;; Добавляем в цель
           existing-target-idx (update-in (conj target-path existing-target-idx :quantity)
                                          #(+ % quantity))
           (not existing-target-idx) (update-in target-path
                                                (fnil conj [])
                                                (assoc moving-item :quantity quantity))))
       ;; Если элемент не найден в источнике
       (do
         (js/console.warn "Item not found in source container:" item-id)
         db)))))

(reg-event-db
 ::add-new-container
 (fn [db _]
   (let [containers (get-in db [:page :containers] {})
         new-id (if (seq containers)
                  (inc (apply max (keys containers)))
                  1)]
     (assoc-in db [:page :containers new-id] []))))

(reg-event-db
 ::remove-item-from-container
 (fn [db [_ container-id item-id]]
   (let [containers-path [:page :containers container-id]
         current-items (vec (get-in db containers-path []))
         item-idx (first (keep-indexed
                          (fn [i entry]
                            (when (= (get-in entry [:item :id]) item-id) i))
                          current-items))]
     (if item-idx
       (let [updated-items (vec (concat (subvec current-items 0 item-idx)
                                        (subvec current-items (inc item-idx))))]
         (if (empty? updated-items)
           ;; Удаляем пустой контейнер
           (update-in db [:page :containers] dissoc container-id)
           ;; Обновляем контейнер
           (assoc-in db containers-path updated-items)))
       db))))
