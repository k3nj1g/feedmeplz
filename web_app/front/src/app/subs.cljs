(ns app.subs
  (:require [re-frame.core  :refer [reg-sub reg-event-fx reg-fx]]
            [re-frame.db    :as db]
            [re-frame.trace :as trace]
            [re-frame.utils :refer [dissoc-in]]
            
            [app.helpers :as h]))

(reg-sub
 ::active-page
 (fn [db _]
   (:active-page db)))

(reg-sub
 :active-popup-menu
 (fn [db [_ section]]
   (-> db :popup-menu (contains? section))))

(reg-sub
 :dialog-state
 (fn [db [_ dialog-id]]
   (get-in db [:dialogs dialog-id])))

(reg-sub
 :http/response
 (fn [db [_ id]]
   (get-in db [:http/response id])))

(reg-sub
 :db/get
 (fn [db [_ path]]
   (let [path' (h/vectorize path)]
     (get-in db path'))))

(reg-fx
 :db/assoc
 (fn [[path val]]
   (let [path' (h/vectorize path)]
     (if-not (identical? (get-in @db/app-db path') val)
       (swap! db/app-db assoc-in path' val)
       (trace/with-trace {:op-type :reagent/quiescent})))))

(reg-fx
 :db/update
 (fn [[path f & args]]
   (let [path' (h/vectorize path)
         nval (apply f (get-in @db/app-db path') args)]
     (if-not (identical? val nval)
       (swap! db/app-db assoc-in path' nval)
       (trace/with-trace {:op-type :reagent/quiescent})))))

(reg-fx
 :db/dissoc
 (fn [path]
   (let [path' (h/vectorize path)]
     (if-not (nil? (get-in @db/app-db path'))
       (swap! db/app-db dissoc-in path')
       (trace/with-trace {:op-type :reagent/quiescent})))))

(reg-event-fx
 :db/assoc
 (fn [_ [_ path val]]
   {:db/assoc [path val]}))

(reg-event-fx
 :db/update
 (fn [_ [_ path f & args]]
   {:db/update (into [path f] args)}))

(reg-event-fx
 :db/dissoc
 (fn [_ [_ path]]
   {:db/dissoc path}))

