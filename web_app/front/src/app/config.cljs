(ns app.config)

(def debug?
  ^boolean goog.DEBUG)

(println "goog.API_URL" goog.API_URL)
(println "API_URL" API_URL)
(println "app.config.API_URL" app.config.API_URL)


(def config
  {:api-url goog.API_URL})

(def api-url (:api-url config))
