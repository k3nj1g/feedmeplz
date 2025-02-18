(ns app.models.abstract-model
  (:require [app.helpers :as h]
            
            [app.models.crud :refer [CRUD]]
            
            [app.server.db :refer [execute-query]]))

(defrecord AbstractModel [table-name schema datasource]
  CRUD
  (create! [_ data]
    (if-let [errors (h/validate-data schema (or data {}))]
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
    (if-let [errors (h/validate-data schema data)]
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

  (list-all [_ {:keys [order-by] :as _query-params}]
    (let [query (cond-> {:select [:*]
                         :from   [table-name]}
                  order-by
                  (assoc :order-by [(keyword order-by)]))]
      (execute-query datasource query))))
