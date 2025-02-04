(ns app.models.user
  (:require [buddy.hashers :as hashers]
            
            [app.models.abstract-model :refer [->AbstractModel]]
            [app.models.crud           :as crud :refer [CRUD]]))

(def Schema
  [:map
   [:username [:string {:min 1, :max 50}]]
   [:email [:string {:min 5, :max 255}]]
   [:telegram_id :string]
   [:password_hash :string]
   [:is_active {:optional true} :boolean]])

(defn- hash-password
  [user]
  (if (:password user)
    (assoc user :password_hash (hashers/derive (:password user)))
    user))

(defn- remove-password
  [user]
  (dissoc user :password))

(defrecord UserModel [datasource]
  CRUD
  (create! [_ data]
    (->> data
         (hash-password)
         (remove-password)
         (crud/create! (->AbstractModel :users Schema datasource))))

  (read [_ id]
    (crud/read (->AbstractModel :users Schema datasource) id))

  (update! [_ id data]
    (-> data
        hash-password
        remove-password
        (crud/update! (->AbstractModel :users Schema datasource) id)))

  (delete! [_ id]
    (crud/delete! (->AbstractModel :users Schema datasource) id))


  (list-all [_ params]
    (crud/list-all (->AbstractModel :users Schema datasource) params)))

(defn model
  [datasource]
  (->UserModel datasource))
