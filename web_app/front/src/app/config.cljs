(ns app.config)

(def debug?
  ^boolean goog.DEBUG)

(def config
  {:api-url goog.API_URL})

(def api-url (:api-url config))
