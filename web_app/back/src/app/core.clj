(ns app.core
  (:require [integrant.core :as ig]

            [app.config :as config]

            [app.server.core])
  (:gen-class))

(defn start
  []
  (ig/init (config/prep)))

(defn stop
  [system]
  (println "Shutdown system")
  (ig/halt! system))

(defn -main
  [& _]
  (let [running-system (start)]
    (println "System started")
    (.addShutdownHook (Runtime/getRuntime) (Thread. ^Runnable #(stop running-system)))))
