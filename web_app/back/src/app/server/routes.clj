(ns app.server.routes
  (:require [reitit.swagger    :as swagger]
            [reitit.swagger-ui :as swagger-ui]

            [reitit.dev.pretty :as pretty]

            [reitit.ring                       :as reitit-ring]
            [reitit.ring.coercion              :as ring-coercion]
            [reitit.ring.middleware.exception  :as exception]
            [reitit.ring.middleware.muuntaja   :as muuntaja]
            [reitit.ring.middleware.parameters :as parameters]

            [reitit.coercion       :as coercion]
            [reitit.coercion.malli :as coercion-malli]

            [ring.middleware.cors :refer [wrap-cors]]

            [malli.util :as mu]

            [muuntaja.core :as m]

            [app.handlers.crud-handler       :as crud]
            [app.handlers.category-handler   :as category-handler]
            [app.handlers.daily-menu-handler :as daily-menu-handler]
            [app.models.category :as category]
            [app.models.dish     :as dish]
            [app.models.order    :as order]
            [app.models.user     :as user]

            [app.server.auth :as auth]))

(defn create-routes [datasource]
  (let [dish-model     (dish/model datasource)
        category-model (category/model datasource)
        user-model     (user/model datasource)
        order-model    (order/model datasource)]
    [;; Публичные маршруты
     ["/swagger.json"
      {:get {:no-doc  true
             :swagger {:info                {:title       "FeedMePlz API"
                                             :description "API для управления меню и заказами"}
                       :securityDefinitions {:auth {:type "apiKey"
                                                    :name "Authorization"
                                                    :in   "header"}}}
             :handler (swagger/create-swagger-handler)}}]

     ["/api-docs/*"
      {:get {:no-doc  true
             :handler (swagger-ui/create-swagger-ui-handler
                       {:url    "/swagger.json"
                        :config {:validatorUrl nil}})}}]

     ["/health"
      {:get {:summary "Проверка работоспособности API"
             :handler (fn [_] {:status 200
                               :body   "ok"})}}]

     ["/auth"
      {:swagger {:tags ["Auth"]}}
      ["/token"
       {:post {:summary    "Получение токена авторизации"
               :parameters {:body [:map
                                   [:username string?]
                                   [:password string?]]}
               :responses  {200 {:body [:map
                                        [:token string?]]}
                            401 {:body [:map
                                        [:error string?]]}}
               :handler    (auth/login-handler datasource)}}]]

     ["/categories"
      {:swagger {:tags ["Categories"]}}
      ["/"
       {:get {:summary "Получение списка всех категорий"
              :handler (crud/list-handler category-model)}}]
      ["/:id"
       {:get {:summary "Получение категории по ID"
              :handler (crud/read-handler category-model)}}]
      ["/:category_id/dishes"
       {:get {:summary "Получение блюд по категории"
              :handler (category-handler/dishes-by-category datasource)}}]]

     ["/dishes"
      {:swagger {:tags ["Dishes"]}}
      ["/"
       {:get {:summary "Получение списка всех блюд"
              :handler (crud/list-handler dish-model)}}]
      ["/:id"
       {:get {:summary "Получение блюда по ID"
              :handler (crud/read-handler dish-model)}}]]

     ["/daily-menus"
      {:swagger {:tags ["Daily menu"]}}
      ["/"
       {:get {:summary "Получение списка ежедневных меню"
              :handler (daily-menu-handler/list-handler datasource)}}]
      ["/:id"
       {:get {:summary "Получение ежедневного меню по ID"
              :handler (daily-menu-handler/read-handler datasource)}}]]

     ;; Защищенные маршруты
     ["/protected"
      {:middleware [auth/wrap-auth]
       :swagger    {:security [{"auth" []}]}}
      ["/categories"
       {:swagger {:tags ["Categories"]}}
       ["/"
        {:post {:summary "Создание новой категории"
                :handler (crud/create-handler category-model)}}]
       ["/:id"
        {:put    {:summary "Обновление категории"
                  :handler (crud/update-handler category-model)}
         :delete {:summary "Удаление категории"
                  :handler (crud/delete-handler category-model)}}]]

      ["/dishes"
       {:swagger {:tags ["Dishes"]}}
       ["/"
        {:post {:summary "Создание нового блюда"
                :handler (crud/create-handler dish-model)}}]
       ["/:id"
        {:put    {:summary "Обновление блюда"
                  :handler (crud/update-handler dish-model)}
         :delete {:summary "Удаление блюда"
                  :handler (crud/delete-handler dish-model)}}]]

      ["/daily-menus"
       {:swagger {:tags ["Daily menu"]}}
       ["/"
        {:post {:summary "Создание нового ежедневного меню"
                :handler (daily-menu-handler/create-handler datasource)}}]
       ["/:id"
        {:put    {:summary "Обновление ежедневного меню"
                  :handler (daily-menu-handler/update-handler datasource)}
         :delete {:summary "Удаление ежедневного меню"
                  :handler (daily-menu-handler/delete-handler datasource)}}]]

      ["/users"
       {:swagger {:tags ["Users"]}}
       ["/"
        {:get  {:summary "Получение списка пользователей"
                :handler (crud/list-handler user-model)}
         :post {:summary "Создание нового пользователя"
                :handler (crud/create-handler user-model)}}]
       ["/:id"
        {:get    {:summary "Получение пользователя по ID"
                  :handler (crud/read-handler user-model)}
         :put    {:summary "Обновление пользователя"
                  :handler (crud/update-handler user-model)}
         :delete {:summary "Удаление пользователя"
                  :handler (crud/delete-handler user-model)}}]]

      ["/orders"
       {:swagger {:tags ["Orders"]}}
       ["/"
        {:get     {:summary "Получение списка заказов"
                   :handler (crud/list-handler order-model)}
         :post    {:summary "Создание нового заказа"
                   :handler (crud/create-handler order-model)}}]
       ["/:id"
        {:get    {:summary "Получение заказа по ID"
                  :handler (crud/read-handler order-model)}
         :put    {:summary "Обновление заказа"
                  :handler (crud/update-handler order-model)}
         :delete {:summary "Удаление заказа"
                  :handler (crud/delete-handler order-model)}}]]]]))

(defn create-app [datasource]
  (wrap-cors
   (reitit-ring/ring-handler
    (reitit-ring/router
     (create-routes datasource)
     {:data {:coercion   coercion-malli/coercion
             :exception  pretty/exception
             :muuntaja   m/instance
             :middleware [swagger/swagger-feature
                          parameters/parameters-middleware
                          muuntaja/format-middleware
                          exception/exception-middleware
                          ring-coercion/coerce-exceptions-middleware
                          ring-coercion/coerce-request-middleware
                          ring-coercion/coerce-response-middleware]
             :compile    coercion/compile-request-coercers
             :validate   mu/closed-schema}})
    (reitit-ring/routes
     (reitit-ring/redirect-trailing-slash-handler)
     (reitit-ring/create-default-handler
      {:not-found (constantly {:status 404, :body "Not Found"})})))
   :access-control-allow-origin  [#"http://localhost:8280"]
   :access-control-allow-methods [:get :post :patch :put :delete]))
