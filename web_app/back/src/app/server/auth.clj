(ns app.server.auth
  (:require [clojure.string :as str]

            [java-time.api      :as jt]
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
  (try
    (let [token (-> request :headers
                    (get "authorization")
                    (str/split #"\s")
                    (last))]
      (jwt/unsign token secret))
    true
    (catch Exception _
      false)))

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

#_(defn check-auth-handler [request]
  (let [token (get-in request [:cookies "auth-token" :value])]
    (if (auth-service/verify-token token)
      (response/response {:authenticated true})
      (-> (response/response {:authenticated false})
          (response/status 401)))))

#_(defn logout-handler [_]
  (-> (response/response {:success true})
      (response/set-cookie "auth-token" ""
                           {:http-only true
                            :secure true
                            :same-site :strict
                            :max-age 0})))  ; Устанавливаем время жизни в 0 для удаления куки
