(ns app.current-menu.controller
  (:require [clojure.string :as str]

            [re-frame.core :refer [reg-event-db reg-event-fx]]
            [tick.core :as t]
            
            [goog.string :as gstr]
            [goog.string.format]
            
            [app.current-menu.form :as form]))

(defn- resize-assignments
  [assignments quantity]
  (let [normalized (->> (or assignments [])
                        (take quantity)
                        (mapv #(when (pos? (or % 0)) %)))
        missing    (max 0 (- quantity (count normalized)))]
    (into normalized (repeat missing nil))))

(defn- parse-int-safe
  [value]
  (if (nil? value)
    nil
    (let [num (js/Number value)]
      (when (and (js/isFinite num)
                 (== num (js/Math.floor num)))
        (int num)))))

(defn format-rub
  [value]
  (let [num (double (or value 0))]
    (if (== num (js/Math.floor num))
      (str (int num))
      (gstr/format "%.2f" num))))

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
   (let [item-id    (:id item)
         old-count  (get-in db [:page :cart item-id] 0)
         new-count  (inc old-count)
         assignments (resize-assignments (get-in db [:page :item-containers item-id]) new-count)]
     (-> db
         (assoc-in [:page :cart item-id] new-count)
         (assoc-in [:page :item-containers item-id] assignments)))))

(reg-event-db
 ::remove-from-cart
 (fn [db [_ item]]
   (let [item-id   (:id item)
         new-count (dec (get-in db [:page :cart item-id] 0))]
     (if (pos? new-count)
       (-> db
           (assoc-in [:page :cart item-id] new-count)
           (assoc-in [:page :item-containers item-id]
                     (resize-assignments (get-in db [:page :item-containers item-id]) new-count)))
       (-> db
           (update-in [:page :cart] dissoc item-id)
           (update-in [:page :item-containers] dissoc item-id))))))

(reg-event-db
 ::set-item-container
 (fn [db [_ item-id portion-index container-number]]
  (let [quantity              (get-in db [:page :cart item-id] 0)
         normalized-index      (parse-int-safe portion-index)
         max-container-number  (get-in db [:page :containers-count] 1)
         normalized-container  (parse-int-safe container-number)
         valid-index?          (and (some? normalized-index)
                                    (<= 0 normalized-index)
                                    (< normalized-index quantity))
         valid-container?      (or (nil? container-number)
                                   (and (some? normalized-container)
                                        (<= 1 normalized-container)
                                        (<= normalized-container max-container-number)))]
     (if (or (zero? quantity) (not valid-index?) (not valid-container?))
       db
       (let [assignments (resize-assignments (get-in db [:page :item-containers item-id]) quantity)]
         (assoc-in db
                   [:page :item-containers item-id]
                   (assoc assignments normalized-index normalized-container)))))))

(reg-event-db
 ::toggle-container-mode
 (fn [db _]
   (update-in db [:page :containers-mode] not)))

(reg-event-db
 ::add-container
 (fn [db _]
   (update-in db [:page :containers-count] (fnil inc 1))))

(reg-event-db
 ::remove-container
 (fn [db [_ container-number]]
   (let [current-count (max 1 (get-in db [:page :containers-count] 1))
         requested     (parse-int-safe container-number)
         target        (if (and (some? requested)
                                (<= 1 requested)
                                (<= requested current-count))
                         requested
                         current-count)]
     (if (= current-count 1)
       db
       (let [next-count (dec current-count)]
         (-> db
             (assoc-in [:page :containers-count] next-count)
             (update-in [:page :item-containers]
                        (fn [item-containers]
                          (reduce-kv
                           (fn [acc item-id assignments]
                             (assoc acc item-id
                                    (mapv (fn [assigned-container]
                                            (cond
                                              (nil? assigned-container) nil
                                              (= assigned-container target) nil
                                              (> assigned-container target) (dec assigned-container)
                                              :else assigned-container))
                                          assignments)))
                           {}
                           (or item-containers {}))))))))))

(reg-event-db
 ::clear-container-assignments
 (fn [db _]
   (let [cart (get-in db [:page :cart] {})]
     (assoc-in db [:page :item-containers]
               (reduce-kv
                (fn [acc item-id quantity]
                  (assoc acc item-id (vec (repeat quantity nil))))
                {}
                cart)))))

(defn get-portions
  [items-in-cart item-containers]
  (->> items-in-cart
       (mapcat
        (fn [{:keys [id quantity name price]}]
          (let [assignments (resize-assignments (get item-containers id) quantity)]
            (map-indexed
             (fn [portion-index container-number]
               {:portion-key      (str id "-" portion-index)
                :item-id          id
                :portion-index    portion-index
                :name             name
                :price            price
                :container-number container-number})
             assignments))))
       (vec)))

(defn summarize-portions
  [portions]
  (->> (or portions [])
       (group-by :item-id)
       (mapv (fn [[_ grouped-portions]]
               (let [{:keys [item-id name price]} (first grouped-portions)
                     quantity (count grouped-portions)]
                 {:item-id   item-id
                  :name      name
                  :price     price
                  :quantity  quantity
                  :line-total (* quantity price)})))
       (sort-by :name)))

(defn build-container-summary
  [items-in-cart item-containers]
  (let [portions                  (get-portions items-in-cart item-containers)
        grouped                   (group-by :container-number portions)
        container-groups          (->> grouped
                                      (remove (comp nil? key))
                                      (sort-by key)
                                      (mapv (fn [[container-number container-portions]]
                                              {:container-number container-number
                                               :items            (summarize-portions container-portions)})))
        without-container-items   (summarize-portions (get grouped nil))
        total-portions            (count portions)
        assigned-portions         (- total-portions (count (get grouped nil)))]
    {:portions                  portions
     :containers                container-groups
     :without-container-items   without-container-items
     :used-containers-count     (count container-groups)
     :assigned-portions-count   assigned-portions
     :unassigned-portions-count (- total-portions assigned-portions)}))

(defn get-order-summary-text 
  [total items-in-cart item-containers containers-mode]
  (let [{:keys [containers without-container-items]}
        (build-container-summary items-in-cart item-containers)

        block->text (fn [title items]
                      (when (seq items)
                        (str title ":\n"
                             (->> items
                                  (map #(gstr/format
                                         "%s - %d шт. × %s ₽ = %s ₽"
                                         (:name %)
                                         (:quantity %)
                                   (format-rub (:price %))
                                   (format-rub (:line-total %))))
                                  (str/join "\n")))))

        items-lines (->> items-in-cart
                         (map #(gstr/format
                                "%s - %d шт. × %s ₽ = %s ₽"
                                (:name %)
                                (:quantity %)
                                (format-rub (:price %))
                                (format-rub (* (:quantity %) (:price %)))))
                         (str/join "\n"))

        container-lines (->> containers
                             (map #(block->text (str "Контейнер " (:container-number %)) (:items %)))
                             (remove nil?))
        free-lines      (when (seq without-container-items)
                          (->> without-container-items
                               (map #(gstr/format
                                      "%s - %d шт. × %s ₽ = %s ₽"
                                      (:name %)
                                      (:quantity %)
                                      (format-rub (:price %))
                                      (format-rub (:line-total %))))
                               (str/join "\n")))
        sections          (if containers-mode
                            (cond-> container-lines
                              free-lines
                              (conj free-lines))
                            nil)]
    (str (if containers-mode
           (str/join "\n\n" sections)
           (str "Позиции:\n" items-lines))
         "\n\nИтого: "
         (format-rub total)
         " ₽")))

(reg-event-fx
  ::copy-order-to-clipboard
  (fn [_ [_ total items-in-cart item-containers containers-mode]]
    (let [order-summary (get-order-summary-text total
                                                items-in-cart
                                                item-containers
                                                containers-mode)]
      {:fx [[:copy-to-clipboard order-summary]
            [:dispatch [::show-copied-notification]]]})))

(reg-event-fx
 ::show-copied-notification
 (fn [{:keys [db]} _]
   {:db             (assoc-in db [:page :copied] true)
    :dispatch-later [{:ms       2000
                      :dispatch [:db/set [:page :copied] false]}]}))
