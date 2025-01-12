(ns app.server.db
  (:require [integrant.core :as ig]
            [honey.sql      :as sql]
            [next.jdbc      :as jdbc]
            [hikari-cp.core :as hikari]))

(defn create-datasource [db-spec]
  (hikari/make-datasource db-spec))

(defmethod ig/init-key :persistent/database [_ config]
  (create-datasource config))

(defmethod ig/halt-key! :persistent/database [_ datasource]
  (hikari/close-datasource datasource))

(defn get-connection [datasource]
  (jdbc/get-connection datasource))

(defn execute-query [datasource query]
  (with-open [conn (get-connection datasource)]
    (jdbc/execute! conn (sql/format query))))
