(ns app.handler
  (:require [ring.util.response :refer [response]]))

(defn main [request]
  (response "Hello, world!"))
