(ns app.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 ::active-page
 (fn [db _]
   (:active-page db)))

(rf/reg-sub
 :active-popup-menu
 (fn [db [_ section]]
   (-> db :popup-menu (contains? section))))
