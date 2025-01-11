(ns user
  (:require [clojure.tools.namespace.repl :refer [set-refresh-dirs]]
            [integrant.repl :refer [clear go halt prep init reset reset-all]]
            [app.config :as config]
            [app.core]))

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

(println "
Available commands:
(start)      - Start the system
(stop)       - Stop the system
(restart)    - Restart the system
(reset-all!) - Reload changed code and restart the system
")
