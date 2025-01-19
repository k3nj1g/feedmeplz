(ns app.server.routes
  (:require [compojure.core  :refer [routes DELETE GET POST PUT]]
            [compojure.route :refer [not-found]]

            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]

            [app.handler                   :as handler]
            [app.handlers.crud-handler     :as crud]
            [app.handlers.category-handler :as category-handler]

            [app.models.category   :as category]
            [app.models.daily-menu :as daily-menu]
            [app.models.dish       :as dish]
            [app.models.order      :as order]
            [app.models.user       :as user]))

(defn create-routes
  [datasource]
  (let [dish-model       (dish/model datasource)
        category-model   (category/model datasource)
        daily-menu-model (daily-menu/model datasource)
        user-model       (user/model datasource)
        order-model      (order/model datasource)]
    (routes
     (GET "/health" [] "ok")
     (GET "/menu" [] handler/get-menu)
     
     ;; Маршруты для категорий
     (GET "/categories" [] (crud/list-handler category-model))
     (GET "/categories/:id" [] (crud/read-handler category-model))
     (GET "/categories/:category_id/dishes" [] (category-handler/dishes-by-category datasource))
     (POST "/categories" [] (crud/create-handler category-model))
     (PUT "/categories/:id" [] (crud/update-handler category-model))
     (DELETE "/categories/:id" [] (crud/delete-handler category-model))

     ;; Маршруты для блюд
     (GET "/dishes" [] (crud/list-handler dish-model))
     (GET "/dishes/:id" [] (crud/read-handler dish-model))
     (POST "/dishes" [] (crud/create-handler dish-model))
     (PUT "/dishes/:id" [] (crud/update-handler dish-model))
     (DELETE "/dishes/:id" [] (crud/delete-handler dish-model))

     ;; Маршруты для ежедневного меню
     (GET "/daily-menu" [] (crud/list-handler daily-menu-model))
     (GET "/daily-menu/:id" [] (crud/read-handler daily-menu-model))
     (POST "/daily-menu" [] (crud/create-handler daily-menu-model))
     (PUT "/daily-menu/:id" [] (crud/update-handler daily-menu-model))
     (DELETE "/daily-menu/:id" [] (crud/delete-handler daily-menu-model))

     ;; Маршруты для пользователей
     (GET "/users" [] (crud/list-handler user-model))
     (GET "/users/:id" [] (crud/read-handler user-model))
     (POST "/users" [] (crud/create-handler user-model))
     (PUT "/users/:id" [] (crud/update-handler user-model))
     (DELETE "/users/:id" [] (crud/delete-handler user-model))

     ;; Маршруты для заказов
     (GET "/orders" [] (crud/list-handler order-model))
     (GET "/orders/:id" [] (crud/read-handler order-model))
     (POST "/orders" [] (crud/create-handler order-model))
     (PUT "/orders/:id" [] (crud/update-handler order-model))
     (DELETE "/orders/:id" [] (crud/delete-handler order-model))     

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
