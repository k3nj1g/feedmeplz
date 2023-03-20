(ns app.home.view
  (:require [app.routes :as routes]
            
            [app.home.model :as model]))

(defn home-panel []
  [:div "Hello world"])

(defmethod routes/pages :home [] [home-panel])
