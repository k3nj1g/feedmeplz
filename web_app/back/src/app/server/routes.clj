(ns app.server.routes
  (:require [clojure.tools.logging :as log]
            
            [compojure.core  :refer [routes DELETE GET POST PUT]]
            [compojure.route :refer [not-found]]

            [ring.middleware.cors           :refer [wrap-cors]]
            [ring.middleware.json           :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.params         :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]

            [app.handler                     :as handler]
            [app.handlers.crud-handler       :as crud]
            [app.handlers.category-handler   :as category-handler]
            [app.handlers.daily-menu-handler :as daily-menu-handler]

            [app.models.category :as category]
            [app.models.dish     :as dish]
            [app.models.order    :as order]
            [app.models.user     :as user]))

(defn create-routes
  [datasource]
  (let [dish-model       (dish/model datasource)
        category-model   (category/model datasource)
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
     (GET "/daily-menus" [] (daily-menu-handler/list-handler datasource))
     (GET "/daily-menus/:id" [] (daily-menu-handler/read-handler datasource))
     (POST "/daily-menus" [] (daily-menu-handler/create-handler datasource))
     (PUT "/daily-menus/:id" [] (daily-menu-handler/update-handler datasource))
     (DELETE "/daily-menus/:id" [] (daily-menu-handler/delete-handler datasource))

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

(defn wrap-exception-handling
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (log/error e "An error occurred while processing the request")
        {:status 500
         :body   {:error   "Internal Server Error"
                  :message (.getMessage e)}}))))

(defn create-app
  [datasource]
  (-> (create-routes datasource)
      wrap-exception-handling
      wrap-keyword-params
      wrap-params
      wrap-json-response
      (wrap-json-body {:keywords? true})
      (wrap-cors :access-control-allow-origin [#"http://localhost:8280"]
                 :access-control-allow-methods [:get :post :put :delete])))
