(ns app.telegram.utils)

;; Отправка данных в Telegram
(defn send-data [data]
  (let [webapp (.-WebApp js/window.Telegram)]
    (.sendData webapp (js/JSON.stringify (clj->js data)))))

;; Показ уведомления в Telegram
(defn show-popup [title message]
  (let [webapp (.-WebApp js/window.Telegram)]
    (.showPopup webapp
                (clj->js {:title title
                          :message message
                          :buttons [{:type "ok"}]}))))

;; Проверка, запущено ли приложение в Telegram
(defn is-telegram? []
  (and (exists? js/window.Telegram)
       (exists? js/window.Telegram.WebApp)))
 