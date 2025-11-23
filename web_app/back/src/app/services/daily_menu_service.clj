(ns app.services.daily-menu-service
  "Daily Menu service layer - Use Cases for daily menu operations
   Coordinates between HTTP layer and domain models"
  (:require [java-time.api :as jt]
            
            [app.db.core :refer [with-transaction]]
            [app.helpers :as h]

            [app.models.daily-menu      :as daily-menu]
            [app.models.daily-menu-item :as daily-menu-item]
            [app.models.crud            :as crud]
            
            [app.services.helpers :refer [success error not-found validation-error]]))


(defn prepare-menu-item-data
  "Prepare menu item data"
  [data]
  (-> data
      (update :price h/as-double)))

(defn validate-menu-date
  "Validate menu date is not in the past"
  [date]
  (when (jt/before? (jt/local-date date) (jt/local-date))
    "Menu date cannot be earlier than today"))

;; ============================================================================
;; Daily Menu CRUD operations
;; ============================================================================

(defn get-all-menus
  "Get all daily menus with pagination
   Returns: {:success true :data paged-result}"
  [db params]
  (try
    (let [page (Integer/parseInt (:page params "1"))
          limit (Integer/parseInt (:limit params "10"))
          offset (* (dec page) limit)
          total-count (crud/count-all (daily-menu/model db) {})
          menus (crud/list-paginated (daily-menu/model db)
                                     (merge params {:limit limit :offset offset}))
          total-pages (Math/ceil (/ total-count limit))]
      (success {:data menus
                :pagination {:current-page page
                             :total-pages total-pages
                             :total-items total-count
                             :limit limit
                             :has-next (< page total-pages)
                             :has-prev (> page 1)}}))
    (catch Exception _
      (error "Failed to fetch menus"))))

(defn get-menu
  "Get daily menu by id with items
   Returns: {:success true :data menu} or error"
  [db menu-id]
  (if menu-id
    (try
      (if-let [menu (crud/read (daily-menu/model db) menu-id)]
        (success menu)
        (not-found))
      (catch Exception _
        (error "Failed to fetch menu")))
    (error "Missing menu id")))

(defn create-menu
  "Create a new daily menu with items
   Returns: {:success true :data menu} or error"
  [db data]
  (let [{:keys [date dishes] :or {date (jt/local-date)}} data]
    (if-let [date-error (validate-menu-date date)]
      (validation-error date-error)
      (if (and date dishes (seq dishes))
        (try
          (with-transaction [tx db]
            (let [menu (crud/create! (daily-menu/model tx) {:date (jt/local-date date)})
                  menu-items (doall
                              (map
                               (fn [{:keys [price] dish-id :id}]
                                 (crud/create! (daily-menu-item/model tx)
                                               (prepare-menu-item-data
                                                {:daily_menu_id (:id menu)
                                                 :dish_id dish-id
                                                 :price price})))
                               dishes))]
              (success {:menu menu :menu_items menu-items})))
          (catch Exception e
            (error (str "Failed to create menu: " (.getMessage e)))))
        (error "Missing required fields: date, dishes")))))

(defn update-menu
  "Update daily menu and its items
   Returns: {:success true :data menu} or error"
  [db menu-id data]
  (if menu-id
    (let [{:keys [date dishes]} data]
      (if-let [date-error (validate-menu-date date)]
        (validation-error date-error)
        (try
          (with-transaction [tx db]
            (if-let [menu (crud/read (daily-menu/model tx) menu-id)]
              (let [;; Update menu date
                    updated-menu (crud/update! (daily-menu/model tx) menu-id {:date (jt/local-date date)})
                    
                    ;; Process dishes
                    menu-items (doall
                                (map
                                 (fn [{:keys [price item-id] dish-id :id}]
                                   (let [item-data (prepare-menu-item-data
                                                    {:daily_menu_id (h/as-int menu-id)
                                                     :dish_id dish-id
                                                     :price price})]
                                     (if item-id
                                       (crud/update! (daily-menu-item/model tx) item-id item-data)
                                       (crud/create! (daily-menu-item/model tx) item-data))))
                                 dishes))
                    
                    ;; Get all current items
                    current-items (seq (:menu_items menu))
                    new-item-ids (set (map :item-id (filter :item-id dishes)))
                    to-delete (filter #(not (new-item-ids (:id %))) current-items)]
                
                ;; Delete removed items
                (doseq [item to-delete]
                  (crud/delete! (daily-menu-item/model tx) (:id item)))
                
                (success {:menu updated-menu :menu_items menu-items}))
              (not-found)))
          (catch Exception e
            (error (str "Failed to update menu: " (.getMessage e)))))))
    (error "Missing menu id")))

(defn delete-menu
  "Delete daily menu by id
   Returns: {:success true :data message} or error"
  [db menu-id]
  (if menu-id
    (try
      (with-transaction [tx db]
        (if (crud/read (daily-menu/model tx) menu-id)
          (do
            (crud/delete! (daily-menu/model tx) menu-id)
            (success {:message "Menu deleted successfully"}))
          (not-found)))
      (catch Exception e
        (error (str "Failed to delete menu: " (.getMessage e)))))
    (error "Missing menu id")))

;; ============================================================================
;; Menu item operations
;; ============================================================================

(defn add-menu-item
  "Add item to daily menu
   Returns: {:success true :data item} or error"
  [db menu-id item-data]
  (if (and menu-id item-data (:dish_id item-data) (:price item-data))
    (try
      (if (crud/read (daily-menu/model db) menu-id)
        (let [data (prepare-menu-item-data
                    (assoc item-data :daily_menu_id (h/as-int menu-id)))]
          (success (crud/create! (daily-menu-item/model db) data)))
        (not-found))
      (catch Exception e
        (error (str "Failed to add menu item: " (.getMessage e)))))
    (error "Missing required fields: menu_id, dish_id, price")))

(defn remove-menu-item
  "Remove item from daily menu
   Returns: {:success true :data message} or error"
  [db item-id]
  (if item-id
    (try
      (if (crud/read (daily-menu-item/model db) item-id)
        (do
          (crud/delete! (daily-menu-item/model db) item-id)
          (success {:message "Menu item removed successfully"}))
        (not-found))
      (catch Exception e
        (error (str "Failed to remove menu item: " (.getMessage e)))))
    (error "Missing item id")))
