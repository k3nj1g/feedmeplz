(ns app.subs
  (:require [re-frame.core :refer [reg-sub]]))

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

