(ns app.server.migrations
  (:require [integrant.core    :as ig]
            [gungnir.migration :as gmg]))

(defmethod ig/init-key :persistent/migrations
  [& _]
  (let [migrations (gmg/load-resources "migrations")]
    (gmg/migrate! migrations)))
