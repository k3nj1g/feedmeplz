(ns app.handlers.daily-menu-handler
  (:require [java-time.api      :as jt]
            [ring.util.response :as response]
            
            [app.models.crud            :as crud]
            [app.models.daily-menu      :as daily-menu]
            [app.models.daily-menu-item :as daily-menu-item]
            
            [app.helpers :as h]

            [app.server.db :refer [with-transaction]]))

(defn prepare-data
  [data]
  (-> data
      (update :price h/as-double)))

(defn create-handler [datasource]
  (fn [request]
    (let [{:keys [date dishes] :or {date (jt/local-date)}} (:body request)]
      (if (jt/before? (jt/local-date date) (jt/local-date))
        (response/bad-request "Menu date cannot be earlier than the current date")
        (try
          (with-transaction [tx datasource]
            (let [menu       (crud/create! (daily-menu/model tx) {:date (jt/local-date date)})
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
    (let [id   (get-in request [:params :id])
          menu (crud/read (daily-menu/model datasource) id)]
      (if menu
        (response/response menu)
        (response/not-found "Not found")))))

(defn list-handler [datasource]
  (fn [request]
    (let [menus (crud/list-all (daily-menu/model datasource) (:params request))]
      (response/response menus))))

(defn update-handler [datasource]
  (fn [request]
    (let [{:keys [date dishes]} (:body request)]
      (if (jt/before? (jt/local-date date) (jt/local-date))
        (response/bad-request "Menu date cannot be earlier than the current date")
        (try
          (with-transaction [tx datasource]
            (let [menu-id    (get-in request [:params :id])
                  menu-items (doall
                              (map
                               (fn [{:keys [price item-id] dish-id :id :as d} ]
                                 (let [data (prepare-data {:daily_menu_id (h/as-int menu-id)
                                                           :dish_id       dish-id
                                                           :price         price})]
                                   (prn d)
                                   (if item-id
                                     (crud/update! (daily-menu-item/model tx) item-id data)
                                     (crud/create! (daily-menu-item/model tx) data))))
                               dishes))]
              (response/created "" {:menu (crud/read (daily-menu/model tx) menu-id) :menu_items menu-items})))
          (catch Exception e
            (response/bad-request {:error (.getMessage e)})))))))

(defn delete-handler [datasource]
  (fn [request]
    (let [id (get-in request [:params :id])
          deleted-menu (crud/delete! (daily-menu/model datasource) id)]
      (if deleted-menu
        (response/response deleted-menu)
        (response/not-found "Not found")))))

(defn publish-daily-menu [datasource]
  (fn [request]
    (let [id (get-in request [:params :id])
          published-menu (daily-menu/publish-menu datasource id)]
      (if published-menu
        (response/response published-menu)
        (response/not-found "Not found")))))

(defn unpublish-daily-menu [datasource]
  (fn [request]
    (let [id (get-in request [:params :id])
          unpublished-menu (daily-menu/unpublish-menu datasource id)]
      (if unpublished-menu
        (response/response unpublished-menu)
        (response/not-found "Not found")))))
