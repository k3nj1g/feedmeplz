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

            [ring.middleware.cors           :refer  [wrap-cors]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.cookies        :refer [wrap-cookies]]

            [malli.util :as mu]

            [muuntaja.core :as m]

            [app.handlers.crud-handler       :as crud]
            [app.handlers.category-handler   :as category-handler]
            [app.handlers.daily-menu-handler :as daily-menu-handler]
            [app.handlers.user-handler       :as user-handler]
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

     ["/api/public"
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
                :handler    (auth/login-handler datasource)}}]
       ["/refresh-token"
        {:post {:summary   "Обновление токена доступа"
                :responses {200 {:body [:map
                                        [:token string?]]}
                            401 {:body [:map
                                        [:error string?]]}}
                :handler   (auth/refresh-token-handler datasource)}}]]

      ["/categories"
       {:swagger {:tags ["Categories"]}}
       [""
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
       [""
        {:get {:summary "Получение списка всех блюд"
               :handler (crud/list-handler dish-model)}}]
       ["/:id"
        {:get {:summary "Получение блюда по ID"
               :handler (crud/read-handler dish-model)}}]]

      ["/daily-menus"
       {:swagger {:tags ["Daily menu"]}}
       [""
        {:get {:summary "Получение списка ежедневных меню"
               :handler (daily-menu-handler/list-handler datasource)}}]
       ["/:id"
        {:get {:summary "Получение ежедневного меню по ID"
               :handler (daily-menu-handler/read-handler datasource)}}]]]

     ;; Защищенные маршруты
     ["/api"
      {:middleware [auth/wrap-auth]
       :swagger    {:security [{"auth" []}]}}
      ["/categories"
       {:swagger {:tags ["Categories"]}}
       [""
        {:post {:summary "Создание новой категории"
                :handler (crud/create-handler category-model)}}]
       ["/:id"
        {:put    {:summary "Обновление категории"
                  :handler (crud/update-handler category-model)}
         :delete {:summary "Удаление категории"
                  :handler (crud/delete-handler category-model)}}]]

      ["/dishes"
       {:swagger {:tags ["Dishes"]}}
       [""
        {:post {:summary "Создание нового блюда"
                :handler (crud/create-handler dish-model)}}]
       ["/:id"
        {:put    {:summary "Обновление блюда"
                  :handler (crud/update-handler dish-model)}
         :delete {:summary "Удаление блюда"
                  :handler (crud/delete-handler dish-model)}}]]

      ["/daily-menus"
       {:swagger {:tags ["Daily menu"]}}
       [""
        {:post {:summary "Создание нового ежедневного меню"
                :handler (daily-menu-handler/create-handler datasource)}}]
       ["/:id"
        {:put    {:summary "Обновление ежедневного меню"
                  :handler (daily-menu-handler/update-handler datasource)}
         :delete {:summary "Удаление ежедневного меню"
                  :handler (daily-menu-handler/delete-handler datasource)}}]]

      ["/users"
       {:swagger {:tags ["Users"]}}
       [""
        {:get  {:summary "Получение списка пользователей"
                :handler (crud/list-handler user-model)}
         :post {:summary "Создание нового пользователя"
                :handler (crud/create-handler user-model)}}]
       ["/self/"
        {:get {:summary "Получение информации о текущем пользователе"
               :handler (user-handler/get-self-user datasource)}}]
       ["/:id"
        {:get    {:summary "Получение пользователя по ID"
                  :handler (crud/read-handler user-model)}
         :put    {:summary "Обновление пользователя"
                  :handler (crud/update-handler user-model)}
         :delete {:summary "Удаление пользователя"
                  :handler (crud/delete-handler user-model)}}]]

      ["/orders"
       {:swagger {:tags ["Orders"]}}
       [""
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

(def parameters-middleware
  {:name    ::parameters
   :compile (fn [{:keys [parameters]} _]
              (if (and (some? (:form parameters)) (nil? (:body parameters)))
                {:data {:swagger {:consumes ["application/x-www-form-urlencoded"]}}}
                {}))
   :wrap    wrap-keyword-params})

(def cors-middleware
  {:name    ::cors
   :compile (fn [_ _] {})
   :wrap    wrap-cors})

(def cookies-middleware
  {:name    ::cookies
   :compile (fn [_ _] {})
   :wrap    wrap-cookies})

(defn create-app [datasource]
  (-> (reitit-ring/ring-handler
       (reitit-ring/router
        (create-routes datasource)
        {:data {:coercion   coercion-malli/coercion
                :exception  pretty/exception
                :muuntaja   m/instance
                :middleware [swagger/swagger-feature
                             [cors-middleware
                              :access-control-allow-origin       (re-pattern (str (System/getenv "FRONTEND_URL") "|https://soft-rats-film.loca.lt"))
                              :access-control-allow-methods      [:get :post :patch :put :delete]
                              :access-control-allow-credentials "true"]
                             cookies-middleware
                             parameters/parameters-middleware
                             parameters-middleware
                             muuntaja/format-middleware
                             exception/exception-middleware
                             ring-coercion/coerce-exceptions-middleware
                             ring-coercion/coerce-request-middleware
                             ring-coercion/coerce-response-middleware]
                :compile    coercion/compile-request-coercers
                :validate   mu/closed-schema}})
       (reitit-ring/routes
        (reitit-ring/create-default-handler
         {:not-found (constantly {:status 404, :body "Not Found"})})))))
