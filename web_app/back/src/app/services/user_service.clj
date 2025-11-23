(ns app.services.user-service
  "User service layer - Use Cases and business operations
   Coordinates between HTTP layer and domain models
   Provides unified error handling"
  (:require [app.services.helpers :as helpers]
  
            [app.models.user :as user]))

;; ============================================================================
;; Result helpers
;; ============================================================================

(defn unauthorized
  "Create unauthorized error"
  []
  (helpers/error "Unauthorized" 401))

;; ============================================================================
;; Authentication Use Cases
;; ============================================================================

(defn authenticate-user
  "Authenticate user with username and password
   Returns: {:success true :data user} or {:success false :error message :status 401}"
  [db {:keys [username password]}]
  (if (and username password)
    (if-let [user (user/authenticate db {:username username :password password})]
      (helpers/success user)
      (unauthorized))
    (helpers/error "Missing username or password")))

(defn get-current-user
  "Get current authenticated user by ID
   Returns: {:success true :data user} or error"
  [db user-id]
  (if user-id
    (if-let [user (user/find-by-id db user-id)]
      (helpers/success user)
      (helpers/not-found))
    (unauthorized)))

(defn change-password
  "Change user's password with verification
   Returns: {:success true :data message} or {:success false :error message :status status}"
  [db user-id old-password new-password]
  (if (and user-id old-password new-password)
    (if (< (count new-password) 6)
      (helpers/error "New password must be at least 6 characters")
      (if-let [user (user/find-by-id-with-hash db user-id)]
        (if (user/verify-password old-password (:password_hash user))
          (do
            (user/update! db user-id {:password new-password})
            (helpers/success {:message "Password changed successfully"}))
          (helpers/error "Old password is incorrect" 401))
        (helpers/not-found)))
    (helpers/error "Missing user-id, old_password, or new_password")))

;; ============================================================================
;; User Management Use Cases
;; ============================================================================

(defn activate-user
  "Activate user account
   Returns: {:success true :data user} or error"
  [db user-id]
  (if user-id
    (try
      (helpers/success (user/activate! db user-id))
      (catch Exception _
        (helpers/not-found)))
    (helpers/error "Missing user id")))

(defn deactivate-user
  "Deactivate user account
   Returns: {:success true :data user} or error"
  [db user-id]
  (if user-id
    (try
      (helpers/success (user/deactivate! db user-id))
      (catch Exception _
        (helpers/not-found)))
    (helpers/error "Missing user id")))

(defn make-admin
  "Grant admin privileges to user
   Returns: {:success true :data user} or error"
  [db user-id]
  (if user-id
    (try
      (helpers/success (user/make-admin! db user-id))
      (catch Exception _
        (helpers/not-found)))
    (helpers/error "Missing user id")))

(defn revoke-admin
  "Revoke admin privileges from user
   Returns: {:success true :data user} or error"
  [db user-id]
  (if user-id
    (try
      (helpers/success (user/revoke-admin! db user-id))
      (catch Exception _
        (helpers/not-found)))
    (helpers/error "Missing user id")))

;; ============================================================================
;; Helper queries (used by auth middleware)
;; ============================================================================

(defn find-by-username
  "Find user by username for JWT refresh
   Returns user with password hash (internal use only)"
  [db username]
  (user/find-by-username db username))
