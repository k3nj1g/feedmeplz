(ns app.models.dish
  "Dish model using functional approach"
  (:require [app.db.core :as db]
            [app.helpers :as h]))

;; ============================================================================
;; Schema
;; ============================================================================

(def schema
  [:map
   [:name [:string {:min 1, :max 100}]]
   [:description {:optional true} [:string]]
   [:kcals {:optional true} [:int {:min 0}]]
   [:weight {:optional true} [:string]]
   [:price [:double {:min 0}]]
   [:category_id [:int]]])

;; ============================================================================
;; Data preparation
;; ============================================================================

(defn prepare-data
  "Prepare dish data before saving (type conversions, cleanup)"
  [data]
  (-> data
      (update :price h/as-double)
      (update :kcals h/as-int)
      ;; Remove nil values
      (->> (remove (fn [[k v]] (nil? v)))
           (into {}))))

;; ============================================================================
;; CRUD operations
;; ============================================================================

(defn create!
  "Create a new dish"
  [db data]
  (db/create! db :dishes schema (prepare-data data)))

(defn find-by-id
  "Find dish by id"
  [db id]
  (db/find-by-id db :dishes id))

(defn find-all
  "Find all dishes, optionally filtered"
  ([db] (find-all db {}))
  ([db conditions]
   (db/find-all db :dishes
                (merge {:order-by [:name]} conditions))))

(defn update!
  "Update dish by id"
  [db id data]
  (db/update! db :dishes schema id (prepare-data data)))

(defn delete!
  "Delete dish by id"
  [db id]
  (db/delete! db :dishes id))

(defn paginate
  "Get paginated list of dishes"
  [db params]
  (db/paginate db :dishes (merge {:order-by [:name]} params)))

;; ============================================================================
;; Custom queries
;; ============================================================================

(defn find-by-category
  "Find all dishes in a category"
  [db category-id]
  (db/find-all db :dishes
               {:where    [:= :category_id category-id]
                :order-by [:name]}))

(defn find-with-category
  "Find all dishes with category information joined"
  ([db] (find-with-category db {}))
  ([db {:keys [category-id]}]
   (db/execute db
               (cond-> {:select   [:d.* :c.name :category_name]
                        :from     [[:dishes :d]]
                        :join     [[:categories :c] [:= :d.category_id :c.id]]
                        :order-by [:c.name :d.name]}
                 category-id
                 (assoc :where [:= :d.category_id category-id])))))

(defn search-by-name
  "Search dishes by name (case-insensitive)"
  [db search-term]
  (db/find-all db :dishes
               {:where [:ilike :name (str "%" search-term "%")]
                :order-by [:name]}))

(defn find-in-price-range
  "Find dishes within price range"
  [db min-price max-price]
  (db/find-all db :dishes
               {:where [:and
                        [:>= :price min-price]
                        [:<= :price max-price]]
                :order-by [:price]}))
