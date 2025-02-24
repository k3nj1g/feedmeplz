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
  (fn [{:keys [body-params]}]
    (let [{:keys [date dishes] :or {date (jt/local-date)}} body-params]
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
    (let [id   (get-in request [:path-params :id])
          menu (crud/read (daily-menu/model datasource) id)]
      (if menu
        (response/response menu)
        (response/not-found "Not found")))))

(defn list-handler [datasource]
  (fn [request]
    (let [menus (crud/list-all (daily-menu/model datasource) (:params request))]
      (response/response menus))))

(defn update-handler [datasource]
  (fn [{:keys [body-params] :as request}]
    (let [{:keys [date dishes]} body-params]
      (if (jt/before? (jt/local-date date) (jt/local-date))
        (response/bad-request "Menu date cannot be earlier than the current date")
        (try
          (with-transaction [tx datasource]
            (let [menu-id    (get-in request [:path-params :id])
                  menu       (crud/read (daily-menu/model tx) menu-id)
                  menu-items (doall
                              (map
                               (fn [{:keys [price item-id] dish-id :id}]
                                 (let [data (prepare-data {:daily_menu_id (h/as-int menu-id)
                                                           :dish_id       dish-id
                                                           :price         price})]
                                   (if item-id
                                     (crud/update! (daily-menu-item/model tx) item-id data)
                                     (crud/create! (daily-menu-item/model tx) data))))
                               dishes))
                  to-delete   (->> menu :menu_items
                                   (remove (fn [item]
                                             (some (partial = (:dish_id item)) (map :id dishes)))))]
              (doseq [item to-delete]
                (crud/delete! (daily-menu-item/model tx) (:id item)))

              (response/created "" {:menu (crud/read (daily-menu/model tx) menu-id) :menu_items menu-items})))
          (catch Exception e
            (response/bad-request {:error (.getMessage e)})))))))

(defn delete-handler [datasource]
  (fn [request]
    (let [id (get-in request [:path-params :id])
          deleted-menu (crud/delete! (daily-menu/model datasource) id)]
      (if deleted-menu
        (response/response deleted-menu)
        (response/not-found "Not found")))))
