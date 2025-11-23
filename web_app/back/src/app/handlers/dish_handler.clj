(ns app.handlers.dish-handler
  "Dish HTTP handlers - transforms HTTP requests to service calls
   Delegates business logic to app.services.dish-service"
  (:require [app.services.dish-service :as dish-service]
            [app.handlers.helpers :as helpers]))

;; ============================================================================
;; Basic CRUD handlers
;; ============================================================================

(defn get-all-dishes
  "Get all dishes"
  [db]
  (fn [_]
    (helpers/service-response
      (dish-service/get-all-dishes db))))

(defn get-dish
  "Get dish by id"
  [db]
  (fn [request]
    (let [dish-id (helpers/get-path-param request :id)]
      (helpers/service-response
        (dish-service/get-dish db dish-id)))))

(defn create-dish
  "Create a new dish"
  [db]
  (fn [request]
    (let [data (helpers/get-body-params request)]
      (helpers/service-response
        (dish-service/create-dish db data)))))

(defn update-dish
  "Update dish by id"
  [db]
  (fn [request]
    (let [dish-id (helpers/get-path-param request :id)
          data (helpers/get-body-params request)]
      (helpers/service-response
        (dish-service/update-dish db dish-id data)))))

(defn delete-dish
  "Delete dish by id"
  [db]
  (fn [request]
    (let [dish-id (helpers/get-path-param request :id)]
      (helpers/service-response
        (dish-service/delete-dish db dish-id)))))

(defn paginate-dishes
  "Get paginated list of dishes"
  [db]
  (fn [request]
    (let [params (:query-params request)]
      (helpers/service-response
        (dish-service/paginate-dishes db params)))))

;; ============================================================================
;; Search and filtering handlers
;; ============================================================================

(defn search-dishes
  "Search dishes by name"
  [db]
  (fn [request]
    (let [search-term (get-in request [:query-params "q"])]
      (helpers/service-response
        (dish-service/search-dishes db search-term)))))

(defn get-dishes-by-category
  "Get all dishes in a category"
  [db]
  (fn [request]
    (let [category-id (helpers/get-path-param request :category_id)]
      (helpers/service-response
        (dish-service/get-dishes-by-category db category-id)))))

(defn get-dishes-in-price-range
  "Get dishes within price range"
  [db]
  (fn [request]
    (let [min-price (get-in request [:query-params "min_price"])
          max-price (get-in request [:query-params "max_price"])]
      (helpers/service-response
        (dish-service/get-dishes-in-price-range db min-price max-price)))))

(defn get-dishes-with-category
  "Get all dishes with category information"
  [db]
  (fn [request]
    (let [category-id (get-in request [:query-params "category_id"])]
      (if category-id
        (helpers/service-response
          (dish-service/get-dishes-with-category db category-id))
        (helpers/service-response
          (dish-service/get-dishes-with-category db))))))
