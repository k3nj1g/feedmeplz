(ns app.models.dish
  (:require [app.models.abstract-model :refer [->AbstractModel]]
            [app.models.crud           :as crud :refer [CRUD]]
            
            [app.helpers :as h]))

(def Schema
  [:map
   [:name [:string {:min 1, :max 100}]]
   [:description {:optional true} [:string]]
   [:price [:double {:min 0}]]
   [:category_id [:int]]])

(defn prepare-data
  [data]
  (-> data
      (update :price h/as-double)
      (update :kcals h/as-int)
      (update :weight h/as-int)))

(defrecord DishModel [datasource]
  CRUD
  (create! [_ data]
    (crud/create! (->AbstractModel :dishes Schema datasource) (prepare-data data)))

  (update! [_ id data]
    (crud/update! (->AbstractModel :dishes Schema datasource) id (prepare-data data)))

  (delete! [_ id]
    (crud/delete! (->AbstractModel :dishes Schema datasource) id))

  (read [_ id]
    (crud/read (->AbstractModel :dishes Schema datasource) id))

  (list-all [_ params]
    (crud/list-all (->AbstractModel :dishes Schema datasource) (assoc params :order-by "name"))))

(defn model [datasource]
  (->DishModel datasource))
