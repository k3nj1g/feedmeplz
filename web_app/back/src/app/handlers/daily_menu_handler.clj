(ns app.handlers.daily-menu-handler
  "Daily Menu HTTP handlers - transforms HTTP requests to service calls
   Delegates business logic to app.services.daily-menu-service"
  (:require [app.services.daily-menu-service :as daily-menu-service]
            [app.handlers.helpers :as helpers]))


;; ============================================================================
;; CRUD handlers
;; ============================================================================

(defn get-all-menus
  "Get all daily menus"
  [db]
  (fn [request]
    (let [params (:query-params request)]
      (helpers/service-response
        (daily-menu-service/get-all-menus db params)))))

(defn get-menu
  "Get menu by id"
  [db]
  (fn [request]
    (let [menu-id (helpers/get-path-param request :id)]
      (helpers/service-response
        (daily-menu-service/get-menu db menu-id)))))

(defn create-menu
  "Create a new daily menu with items"
  [db]
  (fn [request]
    (let [data (helpers/get-body-params request)]
      (helpers/service-response
        (daily-menu-service/create-menu db data)))))

(defn update-menu
  "Update daily menu and its items"
  [db]
  (fn [request]
    (let [menu-id (helpers/get-path-param request :id)
          data (helpers/get-body-params request)]
      (helpers/service-response
        (daily-menu-service/update-menu db menu-id data)))))

(defn delete-menu
  "Delete daily menu by id"
  [db]
  (fn [request]
    (let [menu-id (helpers/get-path-param request :id)]
      (helpers/service-response
        (daily-menu-service/delete-menu db menu-id)))))

;; ============================================================================
;; Menu item handlers
;; ============================================================================

(defn add-menu-item
  "Add item to daily menu"
  [db]
  (fn [request]
    (let [menu-id (helpers/get-path-param request :menu_id)
          data (helpers/get-body-params request)]
      (helpers/service-response
        (daily-menu-service/add-menu-item db menu-id data)))))

(defn remove-menu-item
  "Remove item from daily menu"
  [db]
  (fn [request]
    (let [item-id (helpers/get-path-param request :item_id)]
      (helpers/service-response
        (daily-menu-service/remove-menu-item db item-id)))))
