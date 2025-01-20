(ns app.models.dish
  (:require [app.models.abstract-model :refer [->AbstractModel]]
            [app.models.crud           :as crud :refer [CRUD]]))

(def Schema
  [:map
   [:name [:string {:min 1, :max 100}]]
   [:description {:optional true} [:string]]
   [:price [:double {:min 0}]]
   [:category_id [:int]]])

(defrecord DishModel [datasource]
  CRUD
  (create! [_ data]
    (crud/create! (->AbstractModel :dishes Schema datasource)
                  (update data :price parse-double)))

  (update! [_ id data]
    (crud/update! (->AbstractModel :dishes Schema datasource) id data))

  (delete! [_ id]
    (crud/delete! (->AbstractModel :dishes Schema datasource) id))

  (read [_ id]
    (crud/read (->AbstractModel :dishes Schema datasource) id))

  (list-all [_]
    (crud/list-all (->AbstractModel :dishes Schema datasource))))

(defn model [datasource]
  (->DishModel datasource))
