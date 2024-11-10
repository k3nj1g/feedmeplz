(ns app.home.view
  (:require [app.routes :as routes]))

(defn home-panel
  []
  [:div "Главная"])

(defmethod routes/pages :home [] [home-panel])
