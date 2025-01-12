(ns app.models.abstract-model
  (:require [honey.sql :as sql]
            
            [malli.core  :as m]
            [malli.error :as me]
            
            [next.jdbc            :as jdbc]
            [next.jdbc.result-set :as rs]
            
            [app.models.crud :refer [CRUD]]))

(defn validate-data [schema data]
  (let [result (m/validate schema data)]
    (if (true? result)
      nil
      (me/humanize (m/explain schema data)))))

(defrecord AbstractModel [table-name schema datasource]
  CRUD
  (create! [_ data]
    (if-let [errors (validate-data schema (or data {}))]
      (throw (ex-info "Validation failed" {:errors errors}))
      (let [query (sql/format {:insert-into table-name
                               :values      [data]})]
        (jdbc/execute! datasource query))))

  (read [_ id]
    (let [query (sql/format {:select [:*]
                             :from   [table-name]
                             :where  [:= :id [:cast id :integer]]})]
      (jdbc/execute-one! datasource query {:builder-fn rs/as-unqualified-lower-maps})))

  (update! [_ id data]
    (if-let [errors (validate-data schema data)]
      (throw (ex-info "Validation failed" {:errors errors}))
      (let [query (sql/format {:update table-name
                               :set    data
                               :where  [:cast id :integer]})]
        (jdbc/execute! datasource query))))

  (delete! [_ id]
    (let [query (sql/format {:delete-from table-name
                             :where       [:cast id :integer]})]
      (jdbc/execute! datasource query)))

  (list-all [_]
    (let [query (sql/format {:select [:*]
                             :from   [table-name]})]
      (jdbc/execute! datasource query {:builder-fn rs/as-unqualified-lower-maps}))))
