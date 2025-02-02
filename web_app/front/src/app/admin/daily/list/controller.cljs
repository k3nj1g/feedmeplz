(ns app.admin.daily.list.controller
    (:require [re-frame.core :refer [reg-event-fx]]))

(reg-event-fx 
 :admin-daily-list
 (fn [& _]
   {:http/request [{:method :get
                    :uri    "/categories"
                    :pid    ::categories
                    :success {:event ::init-form}}
                   {:method :get
                    :uri    "/daily-menu"
                    :pid    ::daily-menus}]}))
