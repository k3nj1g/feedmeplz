(ns app.models.user
  "User model using functional approach"
  (:require [buddy.hashers :as hashers]
            
            [app.db.core :as db]))

;; ============================================================================
;; Schema
;; ============================================================================

(def schema
  [:map
   [:username [:string {:min 1, :max 50}]]
   [:email [:string {:min 5, :max 255}]]
   [:telegram_id {:optional true} :string]
   [:password_hash :string]
   [:is_active {:optional true} :boolean]
   [:is_staff {:optional true} :boolean]
   [:is_admin {:optional true} :boolean]])

;; Schema for creating user (accepts password instead of password_hash)
(def create-schema
  [:map
   [:username [:string {:min 1, :max 50}]]
   [:email [:string {:min 5, :max 255}]]
   [:password [:string {:min 6}]]
   [:telegram_id {:optional true} :string]
   [:is_active {:optional true} :boolean]
   [:is_staff {:optional true} :boolean]
   [:is_admin {:optional true} :boolean]])

;; ============================================================================
;; Password handling
;; ============================================================================

(defn hash-password
  "Hash password using buddy hashers"
  [password]
  (hashers/derive password))

(defn verify-password
  "Verify password against hash"
  [password hash]
  (:valid (hashers/verify password hash)))

(defn prepare-user-data
  "Prepare user data: hash password if present, remove password field"
  [data]
  (cond-> data
    (:password data)
    (-> (assoc :password_hash (hash-password (:password data)))
        (dissoc :password))))

(defn sanitize-user
  "Remove sensitive fields from user object"
  [user]
  (dissoc user :password_hash))

;; ============================================================================
;; CRUD operations
;; ============================================================================

(defn create!
  "Create a new user. Accepts :password field which will be hashed."
  [db data]
  (let [prepared-data (prepare-user-data data)]
    (-> (db/create! db :users schema prepared-data)
        (sanitize-user))))

(defn find-by-id
  "Find user by id. Returns sanitized user (without password_hash)."
  [db id]
  (when-let [user (db/find-by-id db :users id)]
    (sanitize-user user)))

(defn find-by-id-with-hash
  "Find user by id with password hash (for internal use only)"
  [db id]
  (db/find-by-id db :users id))

(defn find-by-username
  "Find user by username (with password hash for authentication)"
  [db username]
  (db/find-one-by db :users :username username))

(defn find-all
  "Find all users. Returns sanitized users."
  ([db] (find-all db {}))
  ([db conditions]
   (let [users (db/find-all db :users
                            (merge {:order-by [:username]} conditions))]
     (map sanitize-user users))))

(defn update!
  "Update user by id. If :password is provided, it will be hashed."
  [db id data]
  (let [prepared-data (prepare-user-data data)]
    (-> (db/update! db :users schema id prepared-data)
        (sanitize-user))))

(defn delete!
  "Delete user by id"
  [db id]
  (db/delete! db :users id))

(defn paginate
  "Get paginated list of users"
  [db params]
  (let [result (db/paginate db :users (merge {:order-by [:username]} params))]
    (update result :data (fn [users] (map sanitize-user users)))))

;; ============================================================================
;; Authentication & Authorization
;; ============================================================================

(defn authenticate
  "Authenticate user with username and password.
   Returns sanitized user if credentials are valid, nil otherwise."
  [db {:keys [username password]}]
  (when-let [user (find-by-username db username)]
    (when (verify-password password (:password_hash user))
      (sanitize-user user))))

(defn change-password!
  "Change user password. Verifies old password before updating.
   Returns {:success true} or {:error \"message\"}"
  [db user-id old-password new-password]
  (if-let [user (find-by-id-with-hash db user-id)]
    (if (verify-password old-password (:password_hash user))
      (do
        (update! db user-id {:password new-password})
        {:success true})
      {:error "Old password is incorrect"})
    {:error "User not found"}))

;; ============================================================================
;; Custom queries
;; ============================================================================

(defn find-by-email
  "Find user by email"
  [db email]
  (when-let [user (db/find-one-by db :users :email email)]
    (sanitize-user user)))

(defn find-by-telegram-id
  "Find user by telegram_id"
  [db telegram-id]
  (when-let [user (db/find-one-by db :users :telegram_id telegram-id)]
    (sanitize-user user)))

(defn find-active-users
  "Find all active users"
  [db]
  (find-all db {:where [:= :is_active true]}))

(defn find-admins
  "Find all admin users"
  [db]
  (find-all db {:where [:= :is_admin true]}))

(defn find-staff
  "Find all staff users"
  [db]
  (find-all db {:where [:= :is_staff true]}))

(defn activate!
  "Activate user account"
  [db user-id]
  (update! db user-id {:is_active true}))

(defn deactivate!
  "Deactivate user account"
  [db user-id]
  (update! db user-id {:is_active false}))

(defn make-admin!
  "Grant admin privileges to user"
  [db user-id]
  (update! db user-id {:is_admin true :is_staff true}))

(defn revoke-admin!
  "Revoke admin privileges from user"
  [db user-id]
  (update! db user-id {:is_admin false}))

(defn count-users
  "Count users with optional filters"
  ([db] (db/count-records db :users))
  ([db conditions]
   (db/count-records db :users conditions)))
