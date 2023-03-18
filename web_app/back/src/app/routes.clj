(ns app.routes
  (:require [compojure.core  :refer [defroutes GET]]
            [compojure.route :refer [not-found]]
            
            [app.handler :as handler]))

(defroutes app
  (GET "/" [] handler/main)

  (not-found "Page not found"))
