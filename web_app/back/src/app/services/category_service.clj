(ns app.services.category-service
  "Category service layer - Use Cases for category operations
   Coordinates between HTTP layer and domain models"
  (:require [app.models.category :as category]
            [app.models.dish     :as dish]
            
            [app.services.helpers :refer [success error not-found]]))

;; ============================================================================
;; Category operations
;; ============================================================================

(defn get-all-categories
  "Get all categories with optional dish count
   Returns: {:success true :data categories}"
  [db with-count?]
  (try
    (let [categories (if with-count?
                       (category/with-dish-count db)
                       (category/find-all db))]
      (success categories))
    (catch Exception _
      (error "Failed to fetch categories"))))

(defn get-category
  "Get category by id
   Returns: {:success true :data category} or error"
  [db category-id]
  (if category-id
    (try
      (if-let [category (category/find-by-id db category-id)]
        (success category)
        (not-found))
      (catch Exception _
        (error "Failed to fetch category")))
    (error "Missing category id")))

(defn create-category
  "Create a new category
   Returns: {:success true :data category} or error"
  [db data]
  (try
    (success (category/create! db data))
    (catch Exception e
      (error (str "Failed to create category: " (.getMessage e))))))

(defn update-category
  "Update category by id
   Returns: {:success true :data category} or error"
  [db category-id data]
  (if category-id
    (try
      (if (category/find-by-id db category-id)
        (success (category/update! db category-id data))
        (not-found))
      (catch Exception e
        (error (str "Failed to update category: " (.getMessage e)))))
    (error "Missing category id")))

(defn delete-category
  "Delete category by id
   Returns: {:success true :data message} or error"
  [db category-id]
  (if category-id
    (try
      (if (category/find-by-id db category-id)
        (do
          (category/delete! db category-id)
          (success {:message "Category deleted successfully"}))
        (not-found))
      (catch Exception e
        (error (str "Failed to delete category: " (.getMessage e)))))
    (error "Missing category id")))

;; ============================================================================
;; Related data queries
;; ============================================================================

(defn get-dishes-by-category
  "Get all dishes in a specific category
   Returns: {:success true :data dishes} or error"
  [db category-id]
  (if category-id
    (try
      (if (category/find-by-id db category-id)
        (let [dishes (dish/find-all db {:where [:= :category_id [:cast category-id :integer]]
                                        :order-by [[:name :asc]]})]
          (success dishes))
        (not-found))
      (catch Exception _
        (error "Failed to fetch dishes")))
    (error "Missing category id")))

(defn get-category-with-dishes
  "Get category with all its dishes
   Returns: {:success true :data {:category ... :dishes [...]}} or error"
  [db category-id]
  (if category-id
    (try
      (if-let [category (category/find-by-id db category-id)]
        (let [dishes (dish/find-all db {:where [:= :category_id [:cast category-id :integer]]
                                        :order-by [[:name :asc]]})]
          (success {:category category :dishes dishes}))
        (not-found))
      (catch Exception _
        (error "Failed to fetch category with dishes")))
    (error "Missing category id")))
