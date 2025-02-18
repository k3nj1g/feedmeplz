(ns user
  (:require [clojure.tools.namespace.repl :refer [set-refresh-dirs]]

            [integrant.repl       :refer [go halt reset reset-all]]
            [integrant.repl.state :as ig-state]

            [migratus.core :as migratus]

            [app.config :as config]

            [app.core]

            [app.models.crud :as crud]
            [app.models.user :as user-model]))

(integrant.repl/set-prep! #(config/prep))

(set-refresh-dirs "src" "resources")

;; Добавленные функции для удобства использования
(defn start
  "Запуск системы"
  []
  (go))

(defn stop
  "Остановка системы"
  []
  (halt))

(defn restart
  "Перезапуск системы"
  []
  (reset))

(defn reset-all!
  "Полный перезапуск системы, включая перезагрузку измененного кода"
  []
  (reset-all))

(defn create-admin!
  "Создание нового администратора"
  [username email password]
  (let [system     ig-state/system
        datasource (:persistent/database system)
        user-model (user-model/model datasource)
        data       {:username    username
                    :email       email
                    :password    password
                    :telegram_id "k3nj1g"
                    :is_active   true
                    :is_staff    true
                    :is_admin    true}]
    (crud/create! user-model data)))

(defn create-migration
  "Создание новой миграции"
  [name]
  (let [config (:persistent/migrations (config/prep))]
    (migratus/create config name)))

(println "
Available commands:
(start)                                 - Start the system
(stop)                                  - Stop the system
(restart)                               - Restart the system
(reset-all!)                            - Reload changed code and restart the system
(create-admin! username email password) - Create a new admin user
(create-migration \"name\")             - Create a new migration
")
