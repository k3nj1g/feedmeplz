(ns app.db.core
  (:require [dotenv :refer [env]]
            
            [buddy.hashers :as hash]
            
            [gungnir.model     :as gm]
            [gungnir.database  :as gd]
            [gungnir.changeset :as gc]
            [gungnir.query     :as gq]
            [gungnir.migration :as gmg]
            
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]))

(defn init-connection []
  (gungnir.database/make-datasource!
   {:adapter       "postgresql"
    :username      (env "POSTGRES_USER")
    :password      (env "POSTGRES_PASSWORD")
    :database-name "postgres"
    :server-name   "localhost"
    :port-number   5433}))

(def account-model
  [:map {}
   [:account/id {:primary-key true} int?]
   [:account/created-at {:auto true} inst?]
   [:account/updated-at {:auto true} inst?]
   [:account/name string?]
   [:account/nickname string?]
   [:account/password {:before-save [:bcrypt]} [:string {:min 6}]]
   [:account/password-confirmation {:virtual true} [:string {:min 6}]]])

(defmethod gm/before-save :bcrypt [_k v]
  (buddy.hashers/derive v))

(gungnir.model/register!
 {:account account-model})

(def migrations (gmg/load-resources "migrations"))

(def run-migrations []
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
      (gq/save!)))