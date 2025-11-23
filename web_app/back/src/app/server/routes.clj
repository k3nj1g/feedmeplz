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

            [app.handlers.category-handler   :as category-handler]
            [app.handlers.dish-handler       :as dish-handler]
            [app.handlers.daily-menu-handler :as daily-menu-handler]
            [app.handlers.user-handler       :as user-handler]
            [app.handlers.order-handler      :as order-handler]

            [app.server.auth :as auth]))

(defn create-routes
  [db]
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
              :handler    (auth/login-handler db)}}]
     ["/refresh-token"
      {:post {:summary   "Обновление токена доступа"
              :responses {200 {:body [:map
                                      [:token string?]]}
                          401 {:body [:map
                                      [:error string?]]}}
              :handler   (auth/refresh-token-handler db)}}]]

    ["/categories"
     {:swagger {:tags ["Categories"]}}
     [""
      {:get {:summary "Получение списка всех категорий"
             :handler (category-handler/get-all-categories db)}}]
     ["/:id"
      {:get {:summary "Получение категории по ID"
             :handler (category-handler/get-category db)}}]
     ["/:category_id/dishes"
      {:get {:summary "Получение блюд по категории"
             :handler (category-handler/dishes-by-category db)}}]]

    ["dishes"
     {:swagger {:tags ["Dishes"]}}
     [""
      {:get {:summary "Получение списка всех блюд"
             :handler (dish-handler/get-all-dishes db)}}]
     ["/:id"
      {:get {:summary "Получение блюда по ID"
             :handler (dish-handler/get-dish db)}}]
     ["/category/:category_id"
      {:get {:summary "Получение блюд по категории"
             :handler (dish-handler/get-dishes-by-category db)}}]
     ["/search"
      {:get {:summary "Поиск блюд по названию"
             :handler (dish-handler/search-dishes db)}}]
     ["/price-range"
      {:get {:summary "Поиск блюд по диапазону цены"
             :handler (dish-handler/get-dishes-in-price-range db)}}]
     ["/with-category"
      {:get {:summary "Получение блюд с информацией о категории"
             :handler (dish-handler/get-dishes-with-category db)}}]]

    ["/daily-menus"
     {:swagger {:tags ["Daily menu"]}}
     [""
      {:get {:summary "Получение списка ежедневных меню"
             :handler (daily-menu-handler/get-all-menus db)}}]
     ["/:id"
      {:get {:summary "Получение ежедневного меню по ID"
             :handler (daily-menu-handler/get-menu db)}}]]
    ["/orders"
     {:swagger {:tags ["Orders"]}}
     [""
      {:get {:summary "Получение списка заказов"
             :handler (order-handler/get-all-orders db)}}]
     ["/pending"
      {:get {:summary "Получение всех активных заказов"
             :handler (order-handler/get-pending-orders db)}}]
     ["/completed"
      {:get {:summary "Получение завершенных заказов"
             :handler (order-handler/get-completed-orders db)}}]
     ["/user/:user_id"
      {:get {:summary "Получение заказов пользователя"
             :handler (order-handler/get-user-orders db)}}]
     ["/user/:user_id/summary"
      {:get {:summary "Статистика заказов пользователя"
             :handler (order-handler/get-user-order-summary db)}}]]]

   ;; Защищенные маршруты
   ["/api"
    {:middleware [auth/wrap-auth]
     :swagger    {:security [{"auth" []}]}}
    ["/categories"
     {:swagger {:tags ["Categories"]}}
     [""
      {:post {:summary "Создание новой категории"
              :handler (category-handler/create-category db)}}]
     ["/:id"
      {:put    {:summary "Обновление категории"
                :handler (category-handler/update-category db)}
       :delete {:summary "Удаление категории"
                :handler (category-handler/update-category db)}}]]

    ["/dishes"
     {:swagger {:tags ["Dishes"]}}
     [""
      {:post {:summary "Создание нового блюда"
              :handler (dish-handler/create-dish db)}}]
     ["/:id"
      {:put    {:summary "Обновление блюда"
                :handler (dish-handler/update-dish db)}
       :delete {:summary "Удаление блюда"
                :handler (dish-handler/delete-dish db)}}]]

    ["/daily-menus"
     {:swagger {:tags ["Daily menu"]}}
     [""
      {:post {:summary "Создание нового ежедневного меню"
              :handler (daily-menu-handler/create-menu db)}}]
     #_["/import/validate"
        {:post {:summary "Валидация импорта меню из Excel файла"
                :parameters {:multipart [:map [:file any?]]}
                :handler (daily-menu-handler/validate-import-handler db)}}]
     #_["/import/execute"
        {:post {:summary "Выполнение импорта меню"
                :parameters {:body [:map
                                    [:validation-result [:map
                                                         [:date any?]
                                                         [:status [:enum "valid" "invalid"]]
                                                         [:existing-dishes [:vector any?]]
                                                         [:new-dishes [:vector any?]]]]
                                    [:create-new-dishes {:optional true} boolean?]]}
                :handler (daily-menu-handler/execute-import-handler db)}}]
     ["/:id"
      {:put    {:summary "Обновление ежедневного меню"
                :handler (daily-menu-handler/update-menu db)}
       :delete {:summary "Удаление ежедневного меню"
                :handler (daily-menu-handler/delete-menu db)}}]
     ["/:menu_id/items"
      {:post {:summary "Добавить блюдо в меню"
              :handler (daily-menu-handler/add-menu-item db)}}]
     ["/:menu_id/items/:item_id"
      {:delete {:summary "Удалить блюдо из меню"
                :handler (daily-menu-handler/remove-menu-item db)}}]]
    ["/users"
     ["/self/"
      {:get {:summary "Получение информации о текущем пользователе"
             :handler (user-handler/get-self-handler db)}}]]

    ["/orders"
     {:swagger {:tags ["Orders"]}}
     [""
      {:get     {:summary "Получение списка заказов"
                 :handler (order-handler/get-all-orders db)}
       :post    {:summary "Создание нового заказа"
                 :handler (order-handler/create-order db)}}]
     ["/with-details"
      {:get {:summary "Получение заказов с деталями пользователя и блюда"
             :handler (order-handler/get-orders-with-details db)}}]
     ["/status/:status"
      {:get {:summary "Получение заказов по статусу"
             :handler (order-handler/get-orders-by-status db)}}]
     ["/:id"
      {:get    {:summary "Получение заказа по ID"
                :handler (order-handler/get-order db)}
       :put    {:summary "Обновление заказа"
                :handler (order-handler/update-order db)}
       :delete {:summary "Удаление заказа"
                :handler (order-handler/delete-order db)}}]
     ["/:id/complete"
      {:post {:summary "Завершить заказ"
              :handler (order-handler/complete-order db)}}]
     ["/:id/cancel"
      {:post {:summary "Отменить заказ"
              :handler (order-handler/cancel-order db)}}]]]])

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

(defn create-app [db]
  (-> (reitit-ring/ring-handler
       (reitit-ring/router
        (create-routes db)
        {:data {:coercion   coercion-malli/coercion
                :exception  pretty/exception
                :muuntaja   m/instance
                :middleware [swagger/swagger-feature
                             [cors-middleware
                              :access-control-allow-origin       (re-pattern (System/getenv "FRONTEND_URL"))
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
