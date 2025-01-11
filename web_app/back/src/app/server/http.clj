(ns app.server.http
  (:require [integrant.core     :as ig]
            [org.httpkit.server :as http-server]
            
            [app.server.routes :as routes]))

(defmethod ig/init-key :http/server
  [_ {:keys [config datasource]}]
  (let [app    (routes/create-app datasource)
        server (http-server/run-server app config)]
    (println "INFO: Starting server on port: " (:port config))
    server))

(defmethod ig/halt-key! :http/server
  [_ http-server-instance]
  (println "INFO: Server stopping")
  (http-server-instance :timeout 100))
