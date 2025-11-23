(ns app.handlers.category-handler
  "Category HTTP handlers - transforms HTTP requests to service calls
   Delegates business logic to app.services.category-service"
  (:require [app.services.category-service :as category-service]
            [app.handlers.helpers :as helpers]))

(defn dishes-by-category
  "Returns a list of dishes belonging to the specified category."
  [db]
  (fn [request]
    (let [category-id (helpers/get-path-param request :category_id)]
      (helpers/service-response
        (category-service/get-dishes-by-category db category-id)))))

(defn get-all-categories
  "Get all categories"
  [db]
  (fn [request]
    (let [with-count (= (get-in request [:query-params "with_count"]) "true")]
      (helpers/service-response
        (category-service/get-all-categories db with-count)))))

(defn get-category
  "Get category by id with dishes"
  [db]
  (fn [request]
    (let [category-id (helpers/get-path-param request :id)]
      (helpers/service-response
        (category-service/get-category-with-dishes db category-id)))))

(defn create-category
  "Create a new category"
  [db]
  (fn [request]
    (let [data (helpers/get-body-params request)]
      (helpers/service-response
        (category-service/create-category db data)))))

(defn update-category
  "Update category by id"
  [db]
  (fn [request]
    (let [category-id (helpers/get-path-param request :id)
          data (helpers/get-body-params request)]
      (helpers/service-response
        (category-service/update-category db category-id data)))))

(defn delete-category
  "Delete category by id"
  [db]
  (fn [request]
    (let [category-id (helpers/get-path-param request :id)]
      (helpers/service-response
        (category-service/delete-category db category-id)))))
