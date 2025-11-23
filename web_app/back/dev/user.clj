(ns user
  (:require [clojure.tools.namespace.repl :refer [set-refresh-dirs]]

            [integrant.repl       :refer [go halt reset reset-all]]
            [integrant.repl.state :as ig-state]

            [migratus.core :as migratus]
            [ps]

            [app.config :as config]

            [app.core]

            [app.models.user :as u]))

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
  (let [system ig-state/system
        db     (:persistent/database system)
        data   {:username    username
                :email       email
                :password    password
                :telegram_id "k3nj1g"
                :is_active   true
                :is_staff    true
                :is_admin    true}]
    (u/create! db data)))

(defn create-migration
  "Создание новой миграции"
  [name]
  (let [config (:persistent/migrations (config/prep))]
    (migratus/create config name)))

(defn reset-password!
  "Сброс пароля пользователя по username"
  [username new-password]
  (let [system ig-state/system
        db     (:persistent/database system)
        user   (u/find-by-username db username)]
    
    (if user
      (do
        (u/update! db (:id user) (assoc user :password new-password))
        (println (str "Пароль для пользователя '" username "' успешно изменен")))
      (println (str "Пользователь '" username "' не найден")))))

(println "
Available commands:
(start)                                 - Start the system
(stop)                                  - Stop the system
(restart)                               - Restart the system
(reset-all!)                            - Reload changed code and restart the system
(create-admin! username email password) - Create a new admin user
(reset-password! username new-password) - Reset password for existing user
(create-migration \"name\")             - Create a new migration
")
