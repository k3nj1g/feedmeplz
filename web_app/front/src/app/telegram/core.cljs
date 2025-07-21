(ns app.telegram.core
  "Namespace for Telegram WebApp integration"
  (:require [re-frame.core :as rf]

            [app.telegram.auth   :as telegram-auth]
            [app.telegram.events :as events]
            [app.telegram.utils :as utils])

  (:require-macros [ps]))

;; Инициализация Telegram WebApp
(defn init-telegram-app!
  []
  (ps/persist-scope)
  ;; console.log (params.get ('tgWebAppVersion'));
  (prn "!!!" js/window.location.hash)


  (prn (exists? js/window.Telegram))
  (when true #_(utils/is-telegram?)
    (let [webapp (.-WebApp js/window.Telegram)]
      (.ready webapp)
      (.expand webapp)
      (rf/dispatch-sync [::events/set-telegram-data
                         {:init-data (.-initData webapp)
                          :user     (.-initDataUnsafe webapp)
                          :platform (.-platform webapp)}])
      (rf/dispatch-sync [::telegram-auth/init-telegram-auth]))))
