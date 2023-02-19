(ns app.db.core
  (:require [dotenv :refer [env]]
            
            [buddy.hashers :as hash]
            
            [gungnir.model     :as gm]
            [gungnir.database  :as gd]
            [gungnir.changeset :as gc]
            [gungnir.query     :as gq]
            [gungnir.migration :as gmg]
            
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            
            [app.db.model]))

(defn init-connection []
  (gungnir.database/make-datasource!
   {:adapter       "postgresql"
    :username      (env "POSTGRES_USER")
    :password      (env "POSTGRES_PASSWORD")
    :database-name (or (env "POSTGRES_DB") "postgres")
    :server-name   "localhost"
    :port-number   (or (env "POSTGRES_PORT") 5432)}))

(def migrations (gmg/load-resources "migrations"))

(defn run-migrations []
  (gmg/migrate! migrations))

(comment
  (def ds (jdbc/get-datasource {:dbtype   "postgresql"
                                :port     5433
                                :dbname   "postgres"
                                :password "postgres"
                                :user "postgres"}))
  (sql/query gd/*datasource* ["select 1 as c"])
  
  (-> {:account/name "k3nj1g" :account/nickname "k3nj1g" :account/password "qwerty123"}
      (gc/create)
      (gq/save!))
  
  (run-migrations)

  (gmg/rollback! migrations))
