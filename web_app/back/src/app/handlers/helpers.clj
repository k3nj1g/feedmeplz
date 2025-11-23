(ns app.handlers.helpers
  "Common handler helpers and response utilities"
  (:require [ring.util.response :as response]))

;; ============================================================================
;; Response builders
;; ============================================================================

(defn success-response
  "Build successful response from service result"
  [{:keys [data]}]
  (response/response data))

(defn error-response
  "Build error response from service result"
  [{:keys [error status]}]
  (response/status (response/response {:error error}) (or status 400)))

(defn service-response
  "Convert service result to HTTP response
   Handles both success and error cases"
  [result]
  (if (:success result)
    (success-response result)
    (error-response result)))

;; ============================================================================
;; Request extractors
;; ============================================================================

(defn get-user-id
  "Extract authenticated user ID from request"
  [request]
  (get-in request [:identity :user]))

(defn get-path-param
  "Extract path parameter from request"
  [request param-key]
  (get-in request [:path-params param-key]))

(defn get-body-params
  "Extract body parameters from request"
  [request]
  (:body-params request))

;; ============================================================================
;; Authorization helpers
;; ============================================================================

(defn require-auth
  "Check if user is authenticated
   Returns user-id or nil if not authenticated"
  [request]
  (get-user-id request))

(defn unauthorized-response
  "Return 401 Unauthorized response"
  []
  (response/status (response/response {:error "Unauthorized"}) 401))

(defn not-found-response
  "Return 404 Not Found response"
  [message]
  (response/status (response/response {:error message}) 404))
