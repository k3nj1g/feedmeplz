(ns app.menu.controller
  (:require [re-frame.core :refer [reg-event-fx]]))

(reg-event-fx
 :menu
 (fn [{db :db} _]
   {:http/request {:method :get
                   :uri    "/menu"
                   :pid    :menu}}))

