(ns app.login.model
    (:require [re-frame.core :refer [dispatch]]
              
              [app.login.controller :as ctrl]))

(defn on-submit
  []
  (dispatch [::ctrl/login-flow]))
