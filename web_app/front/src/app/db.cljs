(ns app.db
  (:require [app.config :as config]))

(def default-db
  {:active-page :current-menu
   :popup-menu  #{}
   :dialogs     {}
   :telegram    {:init-data nil
                :user      nil
                :platform  nil}
   :config      {:api-url config/api-url}})
