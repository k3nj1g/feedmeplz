(ns app.handlers.daily-menu-handler
  (:require [ring.util.response :as response]
            [java-time.api :as jt]
            
            [app.models.crud :as crud]
            [app.models.daily-menu :as daily-menu]
            [app.models.daily-menu-item :as daily-menu-item]
            [app.server.db :refer [with-transaction]]

            [app.helpers :as h]))

(defn prepare-data
  [data]
  (-> data
      (update :price h/as-double)))

(defn create-handler [datasource]
  (fn [request]
    (let [{:keys [date dishes] :or {date (jt/local-date)}} (:body request)
          current-date (jt/local-date)]
      (if (jt/before? (jt/local-date date) current-date)
        (response/bad-request "Menu date cannot be earlier than the current date")
        (try
          (with-transaction [tx datasource]
            (let [menu       (crud/create! (daily-menu/model tx) {:date date})
                  menu-items (doall
                              (map
                               (fn [{:keys [price] dish-id :id}]
                                 (crud/create! (daily-menu-item/model tx)
                                               (prepare-data {:daily_menu_id (:id menu)
                                                              :dish_id       dish-id
                                                              :price         price})))
                               dishes))]
              (response/created "" {:menu menu :menu_items menu-items})))
          (catch Exception e
            (response/bad-request {:error (.getMessage e)})))))))

(defn read-handler [datasource]
  (fn [request]
    (let [id   (get-in request [:path-params :id])
          menu (crud/read (daily-menu/model datasource) id)]
      (if menu
        (response/response menu)
        (response/not-found "Not found")))))

(defn list-handler [datasource]
  (fn [request]
    (let [menus (crud/list-all (daily-menu/model datasource) (:query-params request))]
      (response/response menus))))

(defn update-handler [datasource]
  (fn [request]
    (let [id (get-in request [:path-params :id])
          data (:body-params request)
          updated-menu (crud/update! (daily-menu/model datasource) id data)]
      (if updated-menu
        (response/response updated-menu)
        (response/not-found "Not found")))))

(defn delete-handler [datasource]
  (fn [request]
    (let [id (get-in request [:path-params :id])
          deleted-menu (crud/delete! (daily-menu/model datasource) id)]
      (if deleted-menu
        (response/response deleted-menu)
        (response/not-found "Not found")))))

(defn publish-daily-menu [datasource]
  (fn [request]
    (let [id (get-in request [:path-params :id])
          published-menu (daily-menu/publish-menu datasource id)]
      (if published-menu
        (response/response published-menu)
        (response/not-found "Not found")))))

(defn unpublish-daily-menu [datasource]
  (fn [request]
    (let [id (get-in request [:path-params :id])
          unpublished-menu (daily-menu/unpublish-menu datasource id)]
      (if unpublished-menu
        (response/response unpublished-menu)
        (response/not-found "Not found")))))
