(ns app.models.abstract-model
  (:require [malli.core  :as m]
            [malli.error :as me]
            
            [app.models.crud :refer [CRUD]]
            
            [app.server.db :refer [execute-query]]))

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
      (let [query {:insert-into table-name
                   :values      [data]
                   :returning   [:*]}]
        (first (execute-query datasource query)))))

  (read [_ id]
    (let [query {:select [:*]
                 :from   [table-name]
                 :where  [:= :id [:cast id :integer]]}]
      (first (execute-query datasource query))))

  (update! [_ id data]
    (if-let [errors (validate-data schema data)]
      (throw (ex-info "Validation failed" {:errors errors}))
      (let [query {:update    table-name
                   :set       data
                   :where     [:= :id [:cast id :integer]]
                   :returning [:*]}]
        (first (execute-query datasource query)))))

  (delete! [_ id]
    (let [query {:delete-from table-name
                 :where       [:= :id [:cast id :integer]]
                 :returning   [:*]}]
      (first (execute-query datasource query))))

  (list-all [_]
    (let [query {:select [:*]
                 :from   [table-name]}]
      (execute-query datasource query))))
