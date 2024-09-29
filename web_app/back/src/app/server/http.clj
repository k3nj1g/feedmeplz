(ns app.server.http
  (:require [integrant.core     :as ig]
            [org.httpkit.server :as http-server]
            
            [app.server.routes :as routes]))

(defmethod ig/init-key :http/server
  [_ {:keys [port]}]
  (let [server (http-server/run-server #'routes/app {:port port})]
    (println "INFO: Starting server on port: " port)
    server))

(defmethod ig/halt-key! :http/server
  [_ http-server-instance]
  (println "INFO: Server stopping")
  (http-server-instance :timeout 100))
