(ns app.admin.daily.controller
  (:require [re-frame.core :refer [reg-event-fx]]
            
            [app.admin.daily.form :as form]))

(reg-event-fx
 :admin-daily
 (fn [& _]
   {:http/request [{:method :get
                    :uri    "/categories"
                    :pid    ::categories
                    :success {:event ::init-form}}
                   {:method :get
                    :uri    "/dishes"
                    :pid    ::dishes}]}))

(reg-event-fx
 ::init-form
 (fn [_ [_ categories]]
   {:dispatch [:zf/init form/form-path (form/form-schema categories)]}))

(reg-event-fx
 ::save-daily-menu-flow
 (fn []
   {:dispatch [:zf/eval-form form/form-path
               {:success {:event ::create-daily-menu #_(if dish ::update-daily-menu ::create-daily-menu)}}]}))

(reg-event-fx
 ::create-daily-menu
 (fn [_ [_ form-value]]
   {:http/request {:method  :post
                   :uri     "/daily-menu"
                   :body    form-value
                   :success {:event ::save-success}}}))
