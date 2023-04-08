(ns app.home.controller
  (:require [re-frame.core :refer [reg-event-fx]] 
            [ajax.core     :as ajax]
            
            [day8.re-frame.tracing :refer-macros [fn-traced]]))

(reg-event-fx
 :home
 (fn-traced [{db :db} _]
   {:http-xhrio {:method          :get
                 :uri             "http://localhost:8088/menu"
                 :timeout         8000
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:put-response]
                 :on-failure      [:bad-http-result]}}))

