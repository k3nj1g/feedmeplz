(ns app.server.db
  (:require [honey.sql      :as sql]
            [integrant.core :as ig]
            [hikari-cp.core :as hikari]

            [next.jdbc            :as jdbc]
            [next.jdbc.result-set :as rs]

            [app.server.next-jdbc-config]))

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
  `(jdbc/with-transaction [tx# ~datasource]
     (let [~sym tx#]
       ~@body)))
