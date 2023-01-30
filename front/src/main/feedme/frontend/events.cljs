(ns feedme.frontend.events
  (:require [re-frame.core :as rf]
            
            [feedme.frontend.db :as db]))

(rf/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))