(ns app.handlers.user-handler
  (:require [buddy.hashers      :as hashers]
            [ring.util.response :as response]

            [app.server.db :refer [execute-query]]
            [ps]))

(defn find-by-username
  [datasource username]
  (execute-query datasource {:select [:*]
                             :from   [:users]
                             :where  [:= :username username]}))

(defn authenticate-user
  [datasource {:keys [username password]}]
  (when-let [user (first (find-by-username datasource username))]
    (when (hashers/verify password (:password_hash user))
      (dissoc user :password_hash))))

(defn get-self-user
  [datasource]
  (fn [request]
    (ps/persist-scope)
    (if-let [user-id (get-in request [:identity :user])]
      (if-let [user (first (execute-query datasource
                                          {:select [:id :username :email :telegram_id :is_active :is_staff :is_admin]
                                           :from   [:users]
                                           :where  [:= :id user-id]}))]
        (response/response user)
        (-> (response/response {:error "User not found"})
            (response/status 404)))
      (-> (response/response {:error "Unauthorized"})
          (response/status 401)))))
