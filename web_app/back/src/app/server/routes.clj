(ns app.server.routes
  (:require [compojure.core  :refer [defroutes GET POST]]
            [compojure.route :refer [not-found]]
            
            [ring.middleware.json :refer [wrap-json-response]]

            [app.handler :as handler]))

(defroutes routes
  (GET "/health" [] "ok")
  (GET "/menu" [] handler/get-menu)
  (GET "/categories" [] handler/get-categories)
  (POST "/dishes" [] handler/add-dish)
  (POST "/categories" [] handler/add-category)
  (not-found "Not Found"))

(defn allow-cross-origin
  "middleware function to allow crosss origin"
  [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc-in response [:headers "Access-Control-Allow-Origin"] "*"))))

(def app
  (-> routes
      wrap-json-response
      allow-cross-origin))

