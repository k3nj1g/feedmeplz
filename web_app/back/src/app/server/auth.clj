(ns app.server.auth
  (:require [clojure.string :as str]

            [java-time.api      :as jt]
            [buddy.sign.jwt     :as jwt]
            [ring.util.response :as response]

            [app.handlers.user-handler :as user-handler]
            [app.models.crud           :as crud]
            [app.models.user           :as user]))

(def secret (System/getenv "JWT_SECRET"))
(def refresh-token-cookie-name "refresh-token")
(def refresh-token-max-age (* 7 24 60 60)) ; 7 days in seconds

(def development-mode? (= (System/getenv "APP_ENV") "development"))
(def frontend-url (System/getenv "FRONTEND_URL"))

(defn create-token [user]
  (let [claims {:user (:id user)
                :exp  (-> (jt/instant)
                          (jt/plus (jt/hours 1))
                          (jt/to-millis-from-epoch)
                          (/ 1000)
                          (long))}]
    (jwt/sign claims secret)))

(defn create-refresh-token [user]
  (let [claims {:user (:id user)
                :exp  (-> (jt/instant)
                          (jt/plus (jt/days 7))
                          (jt/to-millis-from-epoch)
                          (/ 1000)
                          (long))}]
    (jwt/sign claims secret)))

(defn set-refresh-token-cookie
  [response refresh-token]
  (response/set-cookie response refresh-token-cookie-name refresh-token
                       (cond-> {:http-only true
                                :secure    (if development-mode? false true)
                                :max-age   refresh-token-max-age}
                         (not development-mode?)
                         (assoc :same-site :strict))))

(defn token-claims
  [request]
  (try
    (let [token (-> request :headers
                    (get "authorization")
                    (str/split #"\s")
                    (last))]
      (jwt/unsign token secret))
    (catch Exception _
      nil)))

(defn wrap-auth [handler]
  (fn [request]
    (if-let [claims (token-claims request)]
      (handler (assoc request :identity claims))
      (-> (response/response {:error "Unauthorized"})
          (response/status 401)))))

(defn login-handler
  [datasource]
  (fn [{:keys [body-params]}]
    (if-let [user (user-handler/authenticate-user datasource body-params)]
      (let [token         (create-token user)
            refresh-token (create-refresh-token user)]
        (-> (response/response {:token token})
            (response/status 200)
            (set-refresh-token-cookie refresh-token)
            (response/header "Access-Control-Allow-Credentials" "true")
            (response/header "Access-Control-Allow-Origin" frontend-url)))
      (-> (response/response {:error "Invalid credentials"})
          (response/status 401)))))

(defn refresh-token-handler
  [datasource]
  (fn [request]
    (let [refresh-token (get-in request [:cookies refresh-token-cookie-name :value])]
      (if refresh-token
        (try
          (let [claims  (jwt/unsign refresh-token secret)
                user-id (:user claims)
                user    (crud/read (user/model datasource) user-id)]
            (if user
              (let [new-token         (create-token user)
                    new-refresh-token (create-refresh-token user)]
                (-> (response/response {:token new-token})
                    (response/status 200)
                    (set-refresh-token-cookie new-refresh-token)
                    (response/header "Access-Control-Allow-Credentials" "true")
                    (response/header "Access-Control-Allow-Origin" frontend-url)))
              (-> (response/response {:error "Invalid refresh token"})
                  (response/status 401))))
          (catch Exception _
            (-> (response/response {:error "Invalid refresh token"})
                (response/status 401))))
        (-> (response/response {:error "No refresh token provided"})
            (response/status 401))))))
