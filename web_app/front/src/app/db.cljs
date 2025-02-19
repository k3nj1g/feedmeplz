(ns app.db
  (:require [app.config :as config]))

(def default-db
  {:config {:api-url config/api-url}
   :name   "Feed Me!"})
