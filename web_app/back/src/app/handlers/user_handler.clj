(ns app.handlers.user-handler
  "User HTTP handlers - transforms HTTP requests to service calls
   Delegates business logic to app.services.user-service
   Uses app.handlers.helpers for response formatting"
  (:require [app.handlers.helpers :as helpers]
            
            [app.services.user-service :as user-service]))

;; ============================================================================
;; Authentication Handlers
;; ============================================================================

(defn get-self-handler
  "Get current user info from JWT token"
  [db]
  (fn [request]
    (if-let [user-id (helpers/require-auth request)]
      (helpers/service-response 
       (user-service/get-current-user db user-id))
      (helpers/unauthorized-response))))

(defn change-password-handler
  "Change current user's password"
  [db]
  (fn [request]
    (if-let [user-id (helpers/require-auth request)]
      (let [{:keys [old_password new_password]} (helpers/get-body-params request)]
        (helpers/service-response
          (user-service/change-password db user-id old_password new_password)))
      (helpers/unauthorized-response))))

;; ============================================================================
;; User Management Handlers
;; ============================================================================

(defn activate-handler
  "Activate user account"
  [db]
  (fn [request]
    (let [id (helpers/get-path-param request :id)]
      (helpers/service-response (user-service/activate-user db id)))))

(defn deactivate-handler
  "Deactivate user account"
  [db]
  (fn [request]
    (let [id (helpers/get-path-param request :id)]
      (helpers/service-response (user-service/deactivate-user db id)))))

(defn make-admin-handler
  "Grant admin privileges"
  [db]
  (fn [request]
    (let [id (helpers/get-path-param request :id)]
      (helpers/service-response (user-service/make-admin db id)))))

(defn revoke-admin-handler
  "Revoke admin privileges"
  [db]
  (fn [request]
    (let [id (helpers/get-path-param request :id)]
      (helpers/service-response (user-service/revoke-admin db id)))))

;; ============================================================================
;; Helper functions (used by auth middleware)
;; ============================================================================

(defn authenticate-user
  "Authenticate user with username and password.
   Used by login handler in auth.clj"
  [db credentials]
  (user-service/authenticate-user db credentials))
