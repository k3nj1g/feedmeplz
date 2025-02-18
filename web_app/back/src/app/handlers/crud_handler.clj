(ns app.handlers.crud-handler
  (:require [ring.util.response :as response]

            [app.models.crud :as crud]))

(defn create-handler [model]
  (fn [{:keys [body-params]}]
    (try
      (let [result (crud/create! model body-params)]
        (response/created "" result))
      (catch clojure.lang.ExceptionInfo e
        (response/bad-request (:errors (ex-data e)))))))

(defn read-handler [model]
  (fn [request]
    (let [id     (get-in request [:path-params :id])
          result (crud/read model id)]
      (if result
        (response/response result)
        (response/not-found {:error "Not found"})))))

(defn update-handler [model]
  (fn [{:keys [body-params] :as request}]
    (let [id     (get-in request [:path-params :id])
          result (crud/update! model id body-params)]
      (response/response result))))

(defn delete-handler [model]
  (fn [request]
    (let [id     (get-in request [:path-params :id])
          result (crud/delete! model id)]
      (response/response result))))

(defn list-handler [model]
  (fn [request]
    (let [result (crud/list-all model (:params request))]
      (response/response result))))
