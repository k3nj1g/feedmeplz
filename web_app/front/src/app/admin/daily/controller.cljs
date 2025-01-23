(ns app.admin.daily.controller
  (:require
   [re-frame.core :refer [reg-event-fx]]))

(reg-event-fx
 :admin-daily
 (fn [& _]
   {:http/request [{:method :get
                    :uri    "/categories"
                    :pid    ::categories}
                   {:method :get
                    :uri    "/dishes"
                    :pid    ::categories}]}))
