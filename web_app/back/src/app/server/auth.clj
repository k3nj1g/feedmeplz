(ns app.server.auth
  (:require [java-time.api      :as jt]
            [buddy.sign.jwt     :as jwt]
            [ring.util.response :as response]
            
            [app.handlers.user-handler :as user-handler]))

(def secret (System/getenv "JWT_SECRET"))

(defn create-token [user]
  (let [claims {:user (:id user)
                :exp  (-> (jt/instant)
                          (jt/plus (jt/hours 1))
                          (jt/to-millis-from-epoch)
                          (/ 1000)
                          (long))}]
    (jwt/sign claims secret)))

(defn authenticated?
  [request]
  (let [token (get-in request [:headers "authorization"])]
    (try
      (jwt/unsign token secret)
      true
      (catch Exception _
        false))))

(defn wrap-auth [handler]
  (fn [request]
    (if (authenticated? request)
      (handler request)
      (-> (response/response {:error "Unauthorized"})
          (response/status 401)))))

(defn login-handler
  [datasource]
  (fn [{:keys [body-params]}]
    (if-let [user (user-handler/authenticate-user datasource body-params)]
      (let [token (create-token user)]
        (-> (response/response {:token token})
            (response/status 200)))
      (-> (response/response {:error "Invalid credentials"})
          (response/status 401)))))
