(ns app.models.category
  "Category model using functional approach"
  (:require [app.db.core :as db]))

;; ============================================================================
;; Schema
;; ============================================================================

(def schema
  [:map
   [:name [:string {:min 1, :max 50}]]
   [:description {:optional true} [:string]]])

;; ============================================================================
;; CRUD operations
;; ============================================================================

(defn create!
  "Create a new category"
  [db data]
  (db/create! db :categories schema data))

(defn find-by-id
  "Find category by id"
  [db id]
  (db/find-by-id db :categories id))

(defn find-all
  "Find all categories"
  ([db] (find-all db {}))
  ([db conditions]
   (db/find-all db :categories conditions)))

(defn update!
  "Update category by id"
  [db id data]
  (db/update! db :categories schema id data))

(defn delete!
  "Delete category by id"
  [db id]
  (db/delete! db :categories id))

;; ============================================================================
;; Custom queries
;; ============================================================================

(defn find-by-name
  "Find category by name"
  [db name]
  (db/find-one-by db :categories :name name))

(defn with-dish-count
  "Get categories with count of dishes in each"
  [db]
  (db/execute db
              {:select    [:c.* [[:count :d.id] :dish_count]]
               :from      [[:categories :c]]
               :left-join [[:dishes :d] [:= :d.category_id :c.id]]
               :group-by  [:c.id]
               :order-by  [:c.name]}))
