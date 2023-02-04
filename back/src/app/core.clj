(ns app.core
  (:gen-class)

  (:require [org.httpkit.server :as    app-server]
            [compojure.core     :refer [defroutes GET]]
            [compojure.route    :refer [not-found]]
            [ring.util.response :refer [response]]
            
            [app.handler :as handler]))

(defonce app-server-instance (atom nil))

(defroutes app-routes
  (GET "/" [] handler/main)
  
  (not-found "Page not found"))

(defn -main
  "Start the application server and run the application"
  [& [port]]
  (let [port' (Integer/parseInt (or port "8088")) ]
    (println "INFO: Starting server on port: " port')

    (reset! app-server-instance
            (app-server/run-server #'app-routes {:port port'}))))

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
