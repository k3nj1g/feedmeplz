(ns app.db.core
  "Simple functional database access layer"
  (:require [honey.sql :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]

            [app.helpers :as h]))

;; ============================================================================
;; Core CRUD operations
;; ============================================================================

(defn execute
  "Execute a HoneySQL query and return results"
  [db query]
  (if (instance? java.sql.Connection db)
    (jdbc/execute! db (sql/format query) {:builder-fn rs/as-unqualified-lower-maps})
    (with-open [conn (jdbc/get-connection db)]
      (jdbc/execute! conn (sql/format query) {:builder-fn rs/as-unqualified-lower-maps}))))

(defn create!
  "Insert a new record into table. Validates against schema if provided.
   Returns the created record with generated id."
  ([db table data]
   (create! db table nil data))
  ([db table schema data]
   (when schema
     (when-let [errors (h/validate-data schema data)]
       (throw (ex-info "Validation failed" {:errors errors}))))
   (let [query {:insert-into table
                :values      [data]
                :returning   [:*]}]
     (first (execute db query)))))

(defn find-by-id
  "Find a record by id. Returns nil if not found."
  [db table id]
  (let [query {:select [:*]
               :from   [table]
               :where  [:= :id [:cast id :integer]]}]
    (first (execute db query))))

(defn find-one
  "Find a single record matching conditions.
   Example: (find-one db :users {:where [:= :username \"admin\"]})"
  [db table conditions]
  (let [query (merge {:select [:*] :from [table]} conditions)]
    (first (execute db query))))

(defn find-all
  "Find all records matching optional conditions.
   Example: (find-all db :users {:where [:= :is_admin true]
                                  :order-by [:created_at]})"
  ([db table]
   (find-all db table {}))
  ([db table conditions]
   (let [query (merge {:select [:*] :from [table]} conditions)]
     (execute db query))))

(defn update!
  "Update a record by id. Validates against schema if provided.
   Returns the updated record."
  ([db table id data]
   (update! db table nil id data))
  ([db table schema id data]
   (when schema
     (when-let [errors (h/validate-data schema data)]
       (throw (ex-info "Validation failed" {:errors errors}))))
   (let [query {:update    table
                :set       data
                :where     [:= :id [:cast id :integer]]
                :returning [:*]}]
     (first (execute db query)))))

(defn delete!
  "Delete a record by id. Returns the deleted record."
  [db table id]
  (let [query {:delete-from table
               :where       [:= :id [:cast id :integer]]
               :returning   [:*]}]
    (first (execute db query))))

(defn count-records
  "Count records matching optional conditions."
  ([db table]
   (count-records db table {}))
  ([db table conditions]
   (let [query (merge {:select [[[:count :*] :count]]
                       :from   [table]}
                      conditions)]
     (:count (first (execute db query))))))

(defn paginate
  "Find records with pagination.
   Returns: {:data [...] :pagination {...}}"
  [db table {:keys [page limit order-by where]
             :or   {page     1
                    limit    10
                    order-by [:id]}}]
  (let [offset      (* (dec page) limit)
        total       (count-records db table (when where {:where where}))
        total-pages (Math/ceil (/ total limit))
        data        (find-all db table
                              (cond-> {:limit    limit
                                       :offset   offset
                                       :order-by order-by}
                                where (assoc :where where)))]
    {:data       data
     :pagination {:current-page page
                  :total-pages  total-pages
                  :total-items  total
                  :limit        limit
                  :has-next     (< page total-pages)
                  :has-prev     (> page 1)}}))

;; ============================================================================
;; Transaction support
;; ============================================================================

(defmacro with-transaction
  "Execute body within a database transaction.
   Usage: (with-transaction [tx datasource] ...)"
  [[binding datasource] & body]
  `(jdbc/with-transaction [~binding ~datasource]
     ~@body))

;; ============================================================================
;; Utility functions
;; ============================================================================

(defn exists?
  "Check if a record with given id exists"
  [db table id]
  (boolean (find-by-id db table id)))

(defn find-by
  "Find records by a single field value.
   Example: (find-by db :users :username \"admin\")"
  [db table field value]
  (find-all db table {:where [:= field value]}))

(defn find-one-by
  "Find one record by a single field value"
  [db table field value]
  (first (find-by db table field value)))

(defn delete-where!
  "Delete all records matching conditions.
   Returns count of deleted records."
  [db table where-clause]
  (let [query {:delete-from table
               :where where-clause
               :returning [:id]}
        results (execute db query)]
    (count results)))
