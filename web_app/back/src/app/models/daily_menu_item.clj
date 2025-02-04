(ns app.models.daily-menu-item
  (:require [app.helpers :as h]
            [app.models.crud :as crud :refer [CRUD]]
            [app.server.db :refer [execute-query]]))

(def DailyMenuItemSchema
  [:map
   [:daily_menu_id :int]
   [:dish_id :int]
   [:price :double]])

(defrecord DailyMenuItemModel [table-name schema datasource-or-tx]
  CRUD
  (create! [_ data]
    (if-let [errors (h/validate-data schema (or data {}))]
      (throw (ex-info "Validation failed" {:errors errors}))
      (let [query {:insert-into table-name
                   :values      [data]
                   :returning   [:*]}]
        (first (execute-query datasource-or-tx query)))))

  (read [_ id]
    (let [query {:select [:*]
                 :from   [table-name]
                 :where  [:= :id [:cast id :integer]]}]
      (first (execute-query datasource-or-tx query))))

  (update! [_ id data]
    (if-let [errors (h/validate-data schema data)]
      (throw (ex-info "Validation failed" {:errors errors}))
      (let [query {:update    table-name
                   :set       data
                   :where     [:= :id [:cast id :integer]]
                   :returning [:*]}]
        (first (execute-query datasource-or-tx query)))))

  (delete! [_ id]
    (let [query {:delete-from table-name
                 :where       [:= :id [:cast id :integer]]
                 :returning   [:*]}]
      (first (execute-query datasource-or-tx query))))

  (list-all [_ _params]
    (let [query {:select [:*]
                 :from   [table-name]}]
      (execute-query datasource-or-tx query))))

(defn model [datasource-or-tx]
  (->DailyMenuItemModel :daily_menu_items DailyMenuItemSchema datasource-or-tx))

(defn add-menu-item [datasource-or-tx daily-menu-id dish-id price]
  (let [daily-menu-model (model datasource-or-tx)
        data             {:daily_menu_id daily-menu-id
                          :dish_id       dish-id
                          :price         price}]
    (crud/create! daily-menu-model data)))

(defn remove-menu-item [datasource-or-tx item-id]
  (let [daily-menu-model (model datasource-or-tx)]
    (crud/delete! daily-menu-model item-id)))
