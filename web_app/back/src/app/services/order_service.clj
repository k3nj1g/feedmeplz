(ns app.services.order-service
  "Order service layer - Use Cases for order operations
   Coordinates between HTTP layer and domain models"
  (:require [clojure.string :as str]
            
            [app.models.order :as order]
            [app.models.user  :as user]
            [app.models.dish  :as dish]
            
            [app.services.helpers :refer [success error not-found validation-error]]))

;; ============================================================================
;; Order CRUD operations
;; ============================================================================

(defn get-all-orders
  "Get all orders
   Returns: {:success true :data orders}"
  [db]
  (try
    (success (order/find-all db {}))
    (catch Exception _
      (error "Failed to fetch orders"))))

(defn get-order
  "Get order by id with details
   Returns: {:success true :data order} or error"
  [db order-id]
  (if order-id
    (try
      (if-let [order-data (order/get-order-with-details db order-id)]
        (success order-data)
        (not-found))
      (catch Exception _
        (error "Failed to fetch order")))
    (error "Missing order id")))

(defn create-order
  "Create a new order with validation
   Returns: {:success true :data order} or error"
  [db data]
  (if (and (:user_id data) (:dish_id data) (:quantity data) (:order_date data))
    (try
      ;; Validate user and dish exist
      (if (user/find-by-id db (:user_id data))
        (if (dish/find-by-id db (:dish_id data))
          (success (order/create! db data))
          (error "Dish not found" 404))
        (error "User not found" 404))
      (catch Exception e
        (let [msg (.getMessage e)]
          (if (str/includes? msg "not available")
            (validation-error msg)
            (error (str "Failed to create order: " msg))))))
    (error "Missing required fields: user_id, dish_id, quantity, order_date")))

(defn update-order
  "Update order by id
   Returns: {:success true :data order} or error"
  [db order-id data]
  (if order-id
    (try
      (if (order/find-by-id db order-id)
        (success (order/update! db order-id data))
        (not-found))
      (catch Exception e
        (error (str "Failed to update order: " (.getMessage e)))))
    (error "Missing order id")))

(defn delete-order
  "Delete order by id
   Returns: {:success true :data message} or error"
  [db order-id]
  (if order-id
    (try
      (if (order/find-by-id db order-id)
        (do
          (order/delete! db order-id)
          (success {:message "Order deleted successfully"}))
        (not-found))
      (catch Exception e
        (error (str "Failed to delete order: " (.getMessage e)))))
    (error "Missing order id")))

(defn paginate-orders
  "Get paginated list of orders
   Returns: {:success true :data paged-result}"
  [db params]
  (try
    (let [page (Integer/parseInt (:page params "1"))
          limit (Integer/parseInt (:limit params "10"))]
      (success (order/paginate db {:page page :limit limit})))
    (catch Exception e
      (error (str "Failed to paginate orders: " (.getMessage e))))))

;; ============================================================================
;; Order management operations
;; ============================================================================

(defn complete-order
  "Mark order as completed
   Returns: {:success true :data order} or error"
  [db order-id]
  (if order-id
    (try
      (if (order/find-by-id db order-id)
        (success (order/complete-order! db order-id))
        (not-found))
      (catch Exception e
        (error (str "Failed to complete order: " (.getMessage e)))))
    (error "Missing order id")))

(defn cancel-order
  "Mark order as cancelled
   Returns: {:success true :data order} or error"
  [db order-id]
  (if order-id
    (try
      (if (order/find-by-id db order-id)
        (success (order/cancel-order! db order-id))
        (not-found))
      (catch Exception e
        (error (str "Failed to cancel order: " (.getMessage e)))))
    (error "Missing order id")))

;; ============================================================================
;; Query operations
;; ============================================================================

(defn get-user-orders
  "Get all orders for a user
   Returns: {:success true :data orders} or error"
  [db user-id]
  (if user-id
    (try
      (if (user/find-by-id db user-id)
        (success (order/find-by-user db user-id))
        (not-found))
      (catch Exception _
        (error "Failed to fetch user orders")))
    (error "Missing user id")))

(defn get-orders-by-date
  "Get all orders for a specific date
   Returns: {:success true :data orders} or error"
  [db order-date]
  (if order-date
    (try
      (success (order/find-by-date db order-date))
      (catch Exception _
        (error "Failed to fetch orders by date")))
    (error "Missing order date")))

(defn get-user-orders-by-date
  "Get user's orders for a specific date
   Returns: {:success true :data orders} or error"
  [db user-id order-date]
  (if (and user-id order-date)
    (try
      (if (user/find-by-id db user-id)
        (success (order/find-by-user-and-date db user-id order-date))
        (not-found))
      (catch Exception _
        (error "Failed to fetch user orders for date")))
    (error "Missing user_id or order_date")))

(defn get-orders-by-status
  "Get all orders with specific status
   Returns: {:success true :data orders} or error"
  [db status]
  (if (and status (not (str/blank? status)))
    (try
      (success (order/find-by-status db status))
      (catch Exception _
        (error "Failed to fetch orders by status")))
    (error "Missing or invalid status")))

(defn get-pending-orders
  "Get all pending orders
   Returns: {:success true :data orders}"
  [db]
  (try
    (success (order/find-pending db))
    (catch Exception _
      (error "Failed to fetch pending orders"))))

(defn get-completed-orders
  "Get all completed orders
   Returns: {:success true :data orders}"
  [db]
  (try
    (success (order/find-completed db))
    (catch Exception _
      (error "Failed to fetch completed orders"))))

(defn get-cancelled-orders
  "Get all cancelled orders
   Returns: {:success true :data orders}"
  [db]
  (try
    (success (order/find-cancelled db))
    (catch Exception _
      (error "Failed to fetch cancelled orders"))))

(defn get-orders-with-details
  "Get orders with user and dish details
   Returns: {:success true :data orders}"
  [db]
  (try
    (success (order/get-orders-with-details db {}))
    (catch Exception _
      (error "Failed to fetch orders with details"))))

(defn get-user-order-summary
  "Get user's order summary (total orders, total spent)
   Returns: {:success true :data summary} or error"
  [db user-id]
  (if user-id
    (try
      (if (user/find-by-id db user-id)
        (success (order/get-user-order-summary db user-id))
        (not-found))
      (catch Exception _
        (error "Failed to fetch user order summary")))
    (error "Missing user id")))
