(ns app.server.routes
  (:require [compojure.core  :refer [routes DELETE GET POST PUT]]
            [compojure.route :refer [not-found]]
            
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]

            [app.handler               :as handler]
            [app.handlers.crud-handler :as crud]
            
            [app.models.category :as category]
            [app.models.dish     :as dish]))

(defn create-routes
  [datasource]
  (let [dish-model     (dish/model datasource)
        category-model (category/model datasource)]
    (routes
     (GET "/health" [] "ok")
     (GET "/menu" [] handler/get-menu)
     
     ;; Маршруты для категорий
     (GET "/categories" [] (crud/list-handler category-model))
     (GET "/categories/:id" [] (crud/read-handler category-model))
     (POST "/categories" [] (crud/create-handler category-model))
     (PUT "/categories/:id" [] (crud/update-handler category-model))
     (DELETE "/categories/:id" [] (crud/delete-handler category-model))

     ;; Маршруты для блюд
     (GET "/dishes" [] (crud/list-handler dish-model))
     (GET "/dishes/:id" [] (crud/read-handler dish-model))
     (POST "/dishes" [] (crud/create-handler dish-model))
     (PUT "/dishes/:id" [] (crud/update-handler dish-model))
     (DELETE "/dishes/:id" [] (crud/delete-handler dish-model))

     (not-found "Not Found"))))

(defn allow-cross-origin
  "middleware function to allow cross origin"
  [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc-in response [:headers "Access-Control-Allow-Origin"] "*"))))

(defn create-app
  [datasource]
  (-> (create-routes datasource)
      wrap-json-response
      (wrap-json-body {:keywords? true})
      allow-cross-origin))
