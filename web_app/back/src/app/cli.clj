(ns app.cli
  (:require [integrant.core :as ig]

            [app.config :as config]

            [app.models.crud :as crud]
            [app.models.user :as user-model])
  (:gen-class))

(defn create-admin [username email password]
  (let [system     (ig/init (config/prep #{:http/server}))
        datasource (:persistent/database system)
        user-model (user-model/model datasource)
        data       {:username    username
                    :email       email
                    :password    password
                    :telegram_id "k3nj1g"
                    :is_active   true
                    :is_staff    true
                    :is_admin    true}]
    (crud/create! user-model data)
    (println "Admin user created successfully")
    (ig/halt! system)))

(defn -main [& args]
  (case (first args)
    "create-admin" (if (= (count args) 4)
                     (apply create-admin (rest args))
                     (println "Usage: java -jar your-app.jar create-admin <username> <email> <password>"))
    (println "Unknown command. Available commands: create-admin")))
