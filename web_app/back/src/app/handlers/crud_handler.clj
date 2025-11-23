(ns app.handlers.crud-handler
  "Generic CRUD handlers using functional approach"
  (:require [ring.util.response :as response]))

;; ============================================================================
;; Generic CRUD handlers
;; ============================================================================

(defn create-handler
  "Generic handler for creating a resource.
   model-ns should be a namespace with create! function."
  [db model-ns]
  (fn [{:keys [body-params]}]
    (try
      (let [create-fn (ns-resolve model-ns 'create!)
            result    (create-fn db body-params)]
        (response/created "" result))
      (catch Exception e
        (response/bad-request {:error   (.getMessage e)
                               :details (ex-data e)})))))

(defn read-handler
  "Generic handler for reading a resource by id.
   model-ns should be a namespace with find-by-id function."
  [db model-ns]
  (fn [request]
    (try
      (let [id (get-in request [:path-params :id])
            find-fn (ns-resolve model-ns 'find-by-id)
            result (find-fn db id)]
        (if result
          (response/response result)
          (response/not-found {:error "Not found"})))
      (catch Exception e
        (response/bad-request {:error (.getMessage e)})))))

(defn list-handler
  "Generic handler for listing resources.
   model-ns should be a namespace with find-all function."
  [db model-ns]
  (fn [request]
    (try
      (let [params (:params request)
            find-fn (ns-resolve model-ns 'find-all)
            results (find-fn db params)]
        (response/response results))
      (catch Exception e
        (response/bad-request {:error (.getMessage e)})))))

(defn list-paginated-handler
  "Generic handler for paginated listing.
   model-ns should be a namespace with paginate function."
  [db model-ns]
  (fn [request]
    (try
      (let [params (:params request)
            page (Integer/parseInt (:page params "1"))
            limit (Integer/parseInt (:limit params "10"))
            paginate-fn (ns-resolve model-ns 'paginate)
            result (paginate-fn db {:page page :limit limit})]
        (response/response result))
      (catch Exception e
        (response/bad-request {:error (.getMessage e)})))))

(defn update-handler
  "Generic handler for updating a resource.
   model-ns should be a namespace with update! function."
  [db model-ns]
  (fn [{:keys [body-params] :as request}]
    (try
      (let [id (get-in request [:path-params :id])
            update-fn (ns-resolve model-ns 'update!)
            result (update-fn db id body-params)]
        (if result
          (response/response result)
          (response/not-found {:error "Not found"})))
      (catch Exception e
        (response/bad-request {:error (.getMessage e)
                               :details (ex-data e)})))))

(defn delete-handler
  "Generic handler for deleting a resource.
   model-ns should be a namespace with delete! function."
  [db model-ns]
  (fn [request]
    (try
      (let [id (get-in request [:path-params :id])
            delete-fn (ns-resolve model-ns 'delete!)
            result (delete-fn db id)]
        (if result
          (response/response result)
          (response/not-found {:error "Not found"})))
      (catch Exception e
        (response/bad-request {:error (.getMessage e)})))))
