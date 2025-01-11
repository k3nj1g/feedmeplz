(ns app.server.migrations
  (:require [integrant.core :as ig]
            [migratus.core :as migratus]))

(defmethod ig/init-key :persistent/migrations
  [_ config]
  (migratus/init config)
  (migratus/migrate config))
