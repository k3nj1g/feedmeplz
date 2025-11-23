(ns app.services.dish-service
  "Dish service layer - Use Cases for dish operations
   Coordinates between HTTP layer and domain models"
  (:require [clojure.string :as str]
            
            [app.models.dish     :as dish]
            [app.models.category :as category]
            
            [app.services.helpers :refer [success error not-found]]))

;; ============================================================================
;; Dish operations
;; ============================================================================

(defn get-all-dishes
  "Get all dishes
   Returns: {:success true :data dishes}"
  [db]
  (try
    (success (dish/find-all db))
    (catch Exception _
      (error "Failed to fetch dishes"))))

(defn get-dish
  "Get dish by id
   Returns: {:success true :data dish} or error"
  [db dish-id]
  (if dish-id
    (try
      (if-let [dish-data (dish/find-by-id db dish-id)]
        (success dish-data)
        (not-found))
      (catch Exception _
        (error "Failed to fetch dish")))
    (error "Missing dish id")))

(defn create-dish
  "Create a new dish with category validation
   Returns: {:success true :data dish} or error"
  [db data]
  (try
    ;; Validate category exists
    (if (category/find-by-id db (:category_id data))
      (success (dish/create! db data))
      (error "Category not found" 404))
    (catch Exception e
      (error (str "Failed to create dish: " (.getMessage e))))))

(defn update-dish
  "Update dish by id
   Returns: {:success true :data dish} or error"
  [db dish-id data]
  (if dish-id
    (try
      (if (dish/find-by-id db dish-id)
        ;; Validate category if provided
        (if-let [cat-id (:category_id data)]
          (if (category/find-by-id db cat-id)
            (success (dish/update! db dish-id data))
            (error "Category not found" 404))
          (success (dish/update! db dish-id data)))
        (not-found))
      (catch Exception e
        (error (str "Failed to update dish: " (.getMessage e)))))
    (error "Missing dish id")))

(defn delete-dish
  "Delete dish by id
   Returns: {:success true :data message} or error"
  [db dish-id]
  (if dish-id
    (try
      (if (dish/find-by-id db dish-id)
        (do
          (dish/delete! db dish-id)
          (success {:message "Dish deleted successfully"}))
        (not-found))
      (catch Exception e
        (error (str "Failed to delete dish: " (.getMessage e)))))
    (error "Missing dish id")))

(defn paginate-dishes
  "Get paginated list of dishes
   Returns: {:success true :data paged-result}"
  [db params]
  (try
    (let [page (Integer/parseInt (:page params "1"))
          limit (Integer/parseInt (:limit params "10"))]
      (success (dish/paginate db {:page page :limit limit})))
    (catch Exception e
      (error (str "Failed to paginate dishes: " (.getMessage e))))))

;; ============================================================================
;; Search and filtering
;; ============================================================================

(defn get-dishes-by-category
  "Get all dishes in a category
   Returns: {:success true :data dishes} or error"
  [db category-id]
  (if category-id
    (try
      (if (category/find-by-id db category-id)
        (success (dish/find-by-category db category-id))
        (not-found))
      (catch Exception _
        (error "Failed to fetch dishes")))
    (error "Missing category id")))

(defn search-dishes
  "Search dishes by name
   Returns: {:success true :data dishes} or error"
  [db search-term]
  (if (and search-term (not (str/blank? search-term)))
    (try
      (success (dish/search-by-name db search-term))
      (catch Exception _
        (error "Failed to search dishes")))
    (error "Search term required")))

(defn get-dishes-in-price-range
  "Get dishes within price range
   Returns: {:success true :data dishes} or error"
  [db min-price max-price]
  (if (and min-price max-price)
    (try
      (let [min-p (Double/parseDouble (str min-price))
            max-p (Double/parseDouble (str max-price))]
        (if (<= min-p max-p)
          (success (dish/find-in-price-range db min-p max-p))
          (error "Invalid price range: min_price must be <= max_price")))
      (catch NumberFormatException _
        (error "Invalid price format")))
    (error "Missing min_price or max_price")))

(defn get-dishes-with-category
  "Get all dishes with category information joined
   Returns: {:success true :data dishes}"
  ([db]
   (try
     (success (dish/find-with-category db))
     (catch Exception _
       (error "Failed to fetch dishes"))))
  ([db category-id]
   (if category-id
     (try
       (if (category/find-by-id db category-id)
         (success (dish/find-with-category db {:category-id category-id}))
         (not-found))
       (catch Exception _
         (error "Failed to fetch dishes")))
     (try
       (success (dish/find-with-category db))
       (catch Exception _
         (error "Failed to fetch dishes"))))))
