(ns app.server.db
  (:require [next.jdbc :as jdbc]
            [hikari-cp.core :as hikari]
            [integrant.core :as ig]))

(defn create-datasource [db-spec]
  (prn db-spec)
  (hikari/make-datasource db-spec))

(defmethod ig/init-key :persistent/database [_ config]
  (create-datasource config))

(defmethod ig/halt-key! :persistent/database [_ datasource]
  (hikari/close-datasource datasource))

(defn get-connection [datasource]
  (jdbc/get-connection datasource))

(defn execute-query [datasource sql-params]
  (with-open [conn (get-connection datasource)]
    (jdbc/execute! conn sql-params)))
