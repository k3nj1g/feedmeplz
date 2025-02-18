(ns app.admin.daily.list.controller
    (:require [re-frame.core :refer [reg-event-fx]]))

(reg-event-fx 
 :admin-daily-list
 (fn [& _]
   {:http/request [{:method :get
                    :uri    "/api/public/categories"
                    :pid    ::categories}
                   {:method :get
                    :uri    "/api/public/daily-menus"
                    :pid    ::daily-menus}]}))
