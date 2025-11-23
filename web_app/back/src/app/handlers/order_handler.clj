(ns app.handlers.order-handler
  "Order HTTP handlers - transforms HTTP requests to service calls
   Delegates business logic to app.services.order-service"
  (:require [app.services.order-service :as order-service]
            [app.handlers.helpers :as helpers]))

;; ============================================================================
;; Basic CRUD handlers
;; ============================================================================

(defn get-all-orders
  "Get all orders"
  [db]
  (fn [_]
    (helpers/service-response
      (order-service/get-all-orders db))))

(defn get-order
  "Get order by id with details"
  [db]
  (fn [request]
    (let [order-id (helpers/get-path-param request :id)]
      (helpers/service-response
        (order-service/get-order db order-id)))))

(defn create-order
  "Create a new order"
  [db]
  (fn [request]
    (let [data (helpers/get-body-params request)]
      (helpers/service-response
        (order-service/create-order db data)))))

(defn update-order
  "Update order by id"
  [db]
  (fn [request]
    (let [order-id (helpers/get-path-param request :id)
          data (helpers/get-body-params request)]
      (helpers/service-response
        (order-service/update-order db order-id data)))))

(defn delete-order
  "Delete order by id"
  [db]
  (fn [request]
    (let [order-id (helpers/get-path-param request :id)]
      (helpers/service-response
        (order-service/delete-order db order-id)))))

(defn paginate-orders
  "Get paginated list of orders"
  [db]
  (fn [request]
    (let [params (:query-params request)]
      (helpers/service-response
        (order-service/paginate-orders db params)))))

;; ============================================================================
;; Order management handlers
;; ============================================================================

(defn complete-order
  "Mark order as completed"
  [db]
  (fn [request]
    (let [order-id (helpers/get-path-param request :id)]
      (helpers/service-response
        (order-service/complete-order db order-id)))))

(defn cancel-order
  "Mark order as cancelled"
  [db]
  (fn [request]
    (let [order-id (helpers/get-path-param request :id)]
      (helpers/service-response
        (order-service/cancel-order db order-id)))))

;; ============================================================================
;; Query handlers
;; ============================================================================

(defn get-user-orders
  "Get all orders for a user"
  [db]
  (fn [request]
    (let [user-id (helpers/get-path-param request :user_id)]
      (helpers/service-response
        (order-service/get-user-orders db user-id)))))

(defn get-orders-by-date
  "Get all orders for a specific date"
  [db]
  (fn [request]
    (let [order-date (get-in request [:query-params "date"])]
      (helpers/service-response
        (order-service/get-orders-by-date db order-date)))))

(defn get-user-orders-by-date
  "Get user's orders for a specific date"
  [db]
  (fn [request]
    (let [user-id (helpers/get-path-param request :user_id)
          order-date (get-in request [:query-params "date"])]
      (helpers/service-response
        (order-service/get-user-orders-by-date db user-id order-date)))))

(defn get-orders-by-status
  "Get all orders with specific status"
  [db]
  (fn [request]
    (let [status (get-in request [:query-params "status"])]
      (helpers/service-response
        (order-service/get-orders-by-status db status)))))

(defn get-pending-orders
  "Get all pending orders"
  [db]
  (fn [_]
    (helpers/service-response
      (order-service/get-pending-orders db))))

(defn get-completed-orders
  "Get all completed orders"
  [db]
  (fn [_]
    (helpers/service-response
      (order-service/get-completed-orders db))))

(defn get-cancelled-orders
  "Get all cancelled orders"
  [db]
  (fn [_]
    (helpers/service-response
      (order-service/get-cancelled-orders db))))

(defn get-orders-with-details
  "Get orders with user and dish details"
  [db]
  (fn [_]
    (helpers/service-response
      (order-service/get-orders-with-details db))))

(defn get-user-order-summary
  "Get user's order summary"
  [db]
  (fn [request]
    (let [user-id (helpers/get-path-param request :user_id)]
      (helpers/service-response
        (order-service/get-user-order-summary db user-id)))))
