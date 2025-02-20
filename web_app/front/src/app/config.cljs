(ns app.config)

(def debug?
  ^boolean goog.DEBUG)

(goog-define API_URL "") 

(def config
  {:api-url API_URL})

(def api-url (:api-url config))
