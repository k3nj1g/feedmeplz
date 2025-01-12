(ns app.models.order
  (:require [app.models.abstract-model :refer [->AbstractModel]]
            [app.models.crud           :as crud :refer [CRUD]]
            
            [app.server.db :as db]))

(def Schema
  [:map
   [:user_id :int]
   [:dish_id :int]
   [:quantity :int]
   [:order_date :inst]
   [:status [:string {:max 20}]]])

(defn- check-dish-availability [datasource dish-id order-date]
  (let [result (db/execute-query datasource
                                 {:select [[[:count :*]]]
                                  :from   [:daily_menu]
                                  :where  [:and
                                           [:= :dish_id [:cast dish-id :integer]]
                                           [:= :date order-date]]})]
    (> (:count (first result)) 0)))

(defrecord OrderModel [datasource]
  CRUD
  (create! [_ data]
    (if (check-dish-availability datasource (:dish_id data) (:order_date data))
      (crud/create! (->AbstractModel :orders Schema datasource)
                    (assoc data :status "pending"))
      (throw (ex-info "Dish is not available on this date"
                      {:type :validation-error}))))

  (update! [_ id data]
    (crud/update! (->AbstractModel :orders Schema datasource) id data))

  (delete! [_ id]
    (crud/delete! (->AbstractModel :orders Schema datasource) id))

  (read [_ id]
    (crud/read (->AbstractModel :orders Schema datasource) id))

  (list-all [_]
    (crud/list-all (->AbstractModel :orders Schema datasource))))

(defn model [datasource]
  (->OrderModel datasource))
