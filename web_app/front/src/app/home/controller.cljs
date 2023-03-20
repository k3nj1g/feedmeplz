(ns app.home.controller
  (:require [re-frame.core :refer [reg-event-fx]]
            
            [day8.re-frame.tracing :refer-macros [fn-traced]]))

(reg-event-fx
 :home
 (fn-traced [{db :db} _]
   {}))
