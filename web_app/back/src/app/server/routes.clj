(ns app.server.routes
  (:require [compojure.core  :refer [defroutes GET]]
            [compojure.route :refer [not-found]]
            
            [ring.middleware.json :refer [wrap-json-response]]

            [app.handler :as handler]))

(defroutes routes
  (GET "/health" [] "ok")
  (GET "/menu" [] handler/get-menu)
  (not-found "Page not found"))

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


