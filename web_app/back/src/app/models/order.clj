(ns app.models.order
  "Order model using functional approach"
  (:require [app.db.core :as db]
            [next.jdbc.sql :as sql]
            [honey.sql :as honey]))

;; ============================================================================
;; Schema
;; ============================================================================

(def schema
  [:map
   [:user_id :int]
   [:dish_id :int]
   [:quantity :int]
   [:order_date :inst]
   [:status [:string {:max 20}]]])

;; ============================================================================
;; Helper Functions
;; ============================================================================

(defn- check-dish-availability
  "Check if dish is available in daily menu for the given date"
  [db dish-id order-date]
  (let [query  {:select [[[:count :*] :count]]
                :from   [:daily_menu_items]
                :join   [:daily_menus [:= :daily_menus.id :daily_menu_items.daily_menu_id]]
                :where  [:and
                         [:= :daily_menu_items.dish_id dish-id]
                         [:= :daily_menus.date order-date]]}
        result (db/execute db query)]
    (> (:count (first result)) 0)))

(defn prepare-order-data
  "Prepare order data, setting default status to 'pending' if not provided"
  [data]
  (update data :status #(or % "pending")))

;; ============================================================================
;; CRUD Operations
;; ============================================================================

(defn create!
  "Create a new order. Validates dish availability before creating."
  [db data]
  (let [prepared-data (prepare-order-data data)]
    (if (check-dish-availability db (:dish_id prepared-data) (:order_date prepared-data))
      (db/create! db :orders schema prepared-data)
      (throw (ex-info "Dish is not available on this date"
                      {:type :validation-error
                       :dish-id (:dish_id prepared-data)
                       :order-date (:order_date prepared-data)})))))

(defn find-by-id
  "Find order by ID"
  [db id]
  (db/find-by-id db :orders id))

(defn find-all
  "Find all orders matching conditions, ordered by order_date by default"
  [db conditions]
  (db/find-all db :orders (merge {:order-by [:order_date :desc]} conditions)))

(defn update!
  "Update existing order"
  [db id data]
  (db/update! db :orders schema id data))

(defn delete!
  "Delete order by ID"
  [db id]
  (db/delete! db :orders id))

(defn paginate
  "Paginate orders with default sorting by order_date"
  [db params]
  (db/paginate db :orders (merge {:order-by [:order_date :desc]} params)))

;; ============================================================================
;; Custom Queries
;; ============================================================================

(defn find-by-user
  "Find all orders for a specific user"
  [db user-id]
  (let [query (honey/format
               {:select [:*]
                :from [:orders]
                :where [:= :user_id user-id]
                :order-by [[:order_date :desc]]})]
    (sql/query db query)))

(defn find-by-date
  "Find all orders for a specific date"
  [db order-date]
  (let [query (honey/format
               {:select [:*]
                :from [:orders]
                :where [:= :order_date order-date]
                :order-by [[:created_at :desc]]})]
    (sql/query db query)))

(defn find-by-user-and-date
  "Find all orders for a specific user on a specific date"
  [db user-id order-date]
  (let [query (honey/format
               {:select [:*]
                :from [:orders]
                :where [:and
                        [:= :user_id user-id]
                        [:= :order_date order-date]]
                :order-by [[:created_at :desc]]})]
    (sql/query db query)))

(defn find-by-status
  "Find all orders with a specific status"
  [db status]
  (let [query (honey/format
               {:select [:*]
                :from [:orders]
                :where [:= :status status]
                :order-by [[:order_date :desc]]})]
    (sql/query db query)))

(defn find-pending
  "Find all pending orders"
  [db]
  (find-by-status db "pending"))

(defn find-completed
  "Find all completed orders"
  [db]
  (find-by-status db "completed"))

(defn find-cancelled
  "Find all cancelled orders"
  [db]
  (find-by-status db "cancelled"))

;; ============================================================================
;; Order Management
;; ============================================================================

(defn complete-order!
  "Mark order as completed"
  [db order-id]
  (update! db order-id {:status "completed"}))

(defn cancel-order!
  "Mark order as cancelled"
  [db order-id]
  (update! db order-id {:status "cancelled"}))

(defn get-order-with-details
  "Get order with associated user and dish details"
  [db order-id]
  (let [query (honey/format
               {:select [:orders.*
                         [:users.username :user_username]
                         [:dishes.name :dish_name]
                         [:dishes.price :dish_price]]
                :from [:orders]
                :join [:users [:= :users.id :orders.user_id]
                       :dishes [:= :dishes.id :orders.dish_id]]
                :where [:= :orders.id order-id]})]
    (first (sql/query db query))))

(defn get-orders-with-details
  "Get all orders with associated user and dish details"
  [db conditions]
  (let [where-clause (when (seq conditions)
                       (vec (cons :and
                                  (map (fn [[k v]] [:= (keyword "orders" (name k)) v])
                                       conditions))))
        query (honey/format
               (cond-> {:select [:orders.*
                                 [:users.username :user_username]
                                 [:dishes.name :dish_name]
                                 [:dishes.price :dish_price]]
                        :from [:orders]
                        :join [:users [:= :users.id :orders.user_id]
                               :dishes [:= :dishes.id :orders.dish_id]]
                        :order-by [[:orders.order_date :desc]]}
                 where-clause (assoc :where where-clause)))]
    (sql/query db query)))

(defn get-user-order-summary
  "Get summary of user's orders (total orders, total spent)"
  [db user-id]
  (let [query (honey/format
               {:select [[[:count :orders.id] :total_orders]
                         [[[:sum [:* :orders.quantity :dishes.price]]] :total_spent]]
                :from [:orders]
                :join [:dishes [:= :dishes.id :orders.dish_id]]
                :where [:= :orders.user_id user-id]})]
    (first (sql/query db query))))
