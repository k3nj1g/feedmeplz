(ns app.server.db
  (:require [integrant.core :as ig]
            [honey.sql      :as sql]
            [hikari-cp.core :as hikari]

            [next.jdbc            :as jdbc]
            [next.jdbc.result-set :as rs]))

(defn create-datasource [db-spec]
  (hikari/make-datasource db-spec))

(defmethod ig/init-key :persistent/database [_ config]
  (create-datasource config))

(defmethod ig/halt-key! :persistent/database [_ datasource]
  (hikari/close-datasource datasource))

(defn get-connection [datasource]
  (jdbc/get-connection datasource))

(defn execute-query [datasource-or-conn query]
  (if (instance? java.sql.Connection datasource-or-conn)
    (jdbc/execute! datasource-or-conn (sql/format query) {:builder-fn rs/as-unqualified-lower-maps})
    (with-open [conn (get-connection datasource-or-conn)]
      (jdbc/execute! conn (sql/format query) {:builder-fn rs/as-unqualified-lower-maps}))))

(defmacro with-transaction [[sym datasource] & body]
  `(with-open [conn# (get-connection ~datasource)]
     (jdbc/with-transaction [tx# conn#]
       (let [~sym tx#]
         ~@body))))
