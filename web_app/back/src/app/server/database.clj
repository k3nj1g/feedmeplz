(ns app.server.database
  (:require [gungnir.database  :as gd]
            [gungnir.migration :as gmg]
            [honey.sql         :as hsql]
            [integrant.core    :as ig]))

(defmethod gmg/format-action :feedme/insert-data [[_key {:keys [table columns]} & fields]]
  (-> {:insert-into table
       :columns     columns
       :values      fields}
      (hsql/format)
      (first)))

(defmethod gmg/format-action :feedme/insert-data-reverse [[_key {:keys [table]} & fields]]
  (-> {:delete-from table
       :where [:in :title fields]}
      (hsql/format)
      (first)))

(defmethod ig/init-key :persistent/database
  [_ config]
  (gd/make-datasource! config)
  (println "INFO: Database connection established")
  gd/*datasource*)

(defmethod ig/halt-key! :persistent/database
  [& _]
  (println "INFO: Database connection closed")
  (gd/close!))
