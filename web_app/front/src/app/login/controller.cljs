(ns app.login.controller 
  (:require [re-frame.core :refer [reg-event-fx]]

            [app.auth.events :as auth]

            [app.login.form :as form]))

(reg-event-fx
 :login
 (fn [& _]
   {:dispatch [:zf/init form/form-path form/form-schema]}))

(reg-event-fx
 ::login-flow
 (fn [& _]
   {:dispatch [:zf/eval-form form/form-path
               {:success {:event ::request-auth-token}}]}))

(reg-event-fx
 ::request-auth-token
 (fn [_ [_ {:keys [data]}]]
   {:http/request {:method  :post
                   :uri     "/api/public/auth/token"
                   :body    (:form-value data)
                   :success {:event ::handle-auth-success}}}))

(reg-event-fx
 ::handle-auth-success
 (fn [_ [_ response]]
   {:fx [[:dispatch [::auth/set-authenticated true]]
         [:dispatch [::auth/set-token (:token response)]]
         [:dispatch [:navigate :current-menu]]]}))
