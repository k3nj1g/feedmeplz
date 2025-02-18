(ns app.handlers.user-handler
  (:require [buddy.hashers :as hashers]

            [app.server.db :refer [execute-query]]))

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
