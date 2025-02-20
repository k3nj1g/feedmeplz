(ns app.config)

(def debug?
  ^boolean goog.DEBUG)

(def config
  {:api-url (or goog.API_URL "http://localhost:8090")})

(def api-url (:api-url config))
