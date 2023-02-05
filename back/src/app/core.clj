(ns app.core
  (:gen-class)

  (:require [org.httpkit.server :as app-server]

            [app.routes  :as routes]
            [app.db.core :as db]))

(defonce app-server-instance (atom nil))

(defn -main
  "Start the application server and run the application"
  [& [port]]
  (let [port' (Integer/parseInt (or port "8088"))]
    (println "INFO: Starting server on port: " port')

    (reset! app-server-instance
            (app-server/run-server #'routes/app {:port port'}))

    (db/init-connection)
    (db/run-migrations)))

(defn stop-app-server
  "Gracefully shutdown the server, waiting 100ms"
  []
  (when-not (nil? @app-server-instance)
    (@app-server-instance :timeout 100)
    (reset! app-server-instance nil)
    (println "INFO: Application server stopped")))

(defn restart-app-server
  "Convenience function to stop and start the application server"
  []
  (stop-app-server)
  (-main))

(comment

  ;; start application
  (-main)

  ;; stop application
  (stop-app-server)

  ;; restart application
  (restart-app-server))
