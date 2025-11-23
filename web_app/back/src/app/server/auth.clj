(ns app.server.auth
  (:require [clojure.string :as str]

            [java-time.api         :as jt]
            [buddy.sign.jwt        :as jwt]
            [ring.util.response    :as response]

            [app.services.user-service :as user-service]))

;; ============================================================================
;; Configuration
;; ============================================================================

(def secret (System/getenv "JWT_SECRET"))
(def refresh-token-cookie-name "refresh-token")
(def refresh-token-max-age (* 7 24 60 60)) ; 7 days in seconds

(def development-mode? (= (System/getenv "APP_ENV") "development"))
(def frontend-url (System/getenv "FRONTEND_URL"))

;; ============================================================================
;; Token creation
;; ============================================================================

(defn create-token
  "Create JWT access token (1 hour expiry)"
  [user]
  (let [claims {:user (:id user)
                :exp  (-> (jt/instant)
                          (jt/plus (jt/hours 1))
                          (jt/to-millis-from-epoch)
                          (/ 1000)
                          (long))}]
    (jwt/sign claims secret)))

(defn create-refresh-token
  "Create JWT refresh token (7 days expiry)"
  [user]
  (let [claims {:user (:id user)
                :exp  (-> (jt/instant)
                          (jt/plus (jt/days 7))
                          (jt/to-millis-from-epoch)
                          (/ 1000)
                          (long))}]
    (jwt/sign claims secret)))

;; ============================================================================
;; Cookie management
;; ============================================================================

(defn set-refresh-token-cookie
  "Set refresh token as HTTP-only cookie"
  [response refresh-token]
  (response/set-cookie response refresh-token-cookie-name refresh-token
                       (cond-> {:http-only true
                                :secure    (if development-mode? false true)
                                :max-age   refresh-token-max-age}
                         (not development-mode?)
                         (assoc :same-site :strict))))

;; ============================================================================
;; Token validation
;; ============================================================================

(defn token-claims
  "Extract and validate JWT claims from request"
  [request]
  (try
    (let [token (-> request :headers
                    (get "authorization")
                    (str/split #"\s")
                    (last))]
      (jwt/unsign token secret))
    (catch Exception _
      nil)))

;; ============================================================================
;; Middleware
;; ============================================================================

(defn wrap-auth
  "Middleware to require authentication.
   Adds :identity to request with JWT claims."
  [handler]
  (fn [request]
    (if-let [claims (token-claims request)]
      (handler (assoc request :identity claims))
      (-> (response/response {:error "Unauthorized"})
          (response/status 401)))))

;; ============================================================================
;; Authentication Handlers
;; ============================================================================

(defn login-handler
  "Handle user login with username/password"
  [db]
  (fn [{:keys [body-params]}]
    (let [result (user-service/authenticate-user db body-params)]
      (if (:success result)
        (let [user          (:data result)
              token         (create-token user)
              refresh-token (create-refresh-token user)]
          (-> (response/response {:token token})
              (response/status 200)
              (set-refresh-token-cookie refresh-token)
              (response/header "Access-Control-Allow-Credentials" "true")
              (response/header "Access-Control-Allow-Origin" frontend-url)))
        (-> (response/response {:error "Invalid credentials"})
            (response/status 401))))))

(defn- build-token-response
  "Build a response with JWT access token and refresh token as HTTP-only cookies"
  [user]
  (let [token         (create-token user)
        refresh-token (create-refresh-token user)]
    (-> (response/response {:token token})
        (response/status 200)
        (set-refresh-token-cookie refresh-token)
        (response/header "Access-Control-Allow-Credentials" "true")
        (response/header "Access-Control-Allow-Origin" frontend-url))))

(defn- invalid-token-response 
  "Return 401 Unauthorized response for invalid refresh token"
  []
  (-> (response/response {:error "Invalid refresh token"})
      (response/status 401)))

(defn refresh-token-handler
  "Handle refresh token exchange for new access token"
  [db]
  (fn [request]
    (let [refresh-token (get-in request [:cookies refresh-token-cookie-name :value])]
      (if refresh-token
        (try
          (let [claims  (jwt/unsign refresh-token secret)
                user-id (:user claims)
                result  (user-service/get-current-user db user-id)]
            (if (:success result)
              (build-token-response (:data result))
              (invalid-token-response)))
          (catch Exception _
            (invalid-token-response)))
        (-> (response/response {:error "No refresh token provided"})
            (response/status 401))))))
