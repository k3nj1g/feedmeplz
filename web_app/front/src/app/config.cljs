(ns app.config)

(def debug?
  ^boolean goog.DEBUG)

(goog-define API_URL "") 

(def config
  {:dev? true
   :telegram-bot-token "8106384722:AAFXPENtZbnEfsMtT1_VgeTDidv7IAh-FW8"
   :telegram-bot-username "feedmeplz_test_bot"
   :telegram-mini-app-username "feedmeplz"
   :api-url API_URL})

(def api-url (:api-url config))
