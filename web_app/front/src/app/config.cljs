(ns app.config)

(def debug?
  ^boolean goog.DEBUG)

(goog-define API_URL "") 

(def config
  {:api-url (if (empty? API_URL) "http://localhost:8088" API_URL)})

(def api-url (:api-url config))
