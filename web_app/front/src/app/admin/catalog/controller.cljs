(ns app.admin.catalog.controller 
  (:require
   [re-frame.core :refer [reg-event-fx]]))

(reg-event-fx
 :admin-catalog
 (fn [& _]
   {:http/request {:method  :get
                   :uri     "/categories"
                   :pid     ::categories
                   :success {:event ::set-active-category}}}))

(reg-event-fx
 ::set-active-category
 (fn [{db :db} [_ categories]]
   {:db (assoc-in db [:page :active-category] (first categories))}))

(reg-event-fx
 ::get-dishes-by-category
 (fn [_ [_ category_id]]
   {:http/request {:method :get
                   :uri    (str "/categories/" category_id "/dishes")
                   :pid    ::dishes-by-category}}))
