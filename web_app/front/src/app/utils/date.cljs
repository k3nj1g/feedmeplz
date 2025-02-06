(ns app.utils.date
  (:require-macros [ps]))

(defn ->ru-verbose
  "Форматирует дату в виде строки 'день месяц год' на русском языке.
   Принимает объект Date или строку в формате ISO 8601 (YYYY-MM-DD).
   Возвращает отформатированную строку."
  [date]
  (let [date-obj (if (string? date)
                   (js/Date. date)
                   date)]
    (ps/persist-scope)
    (.toLocaleDateString date-obj "ru-RU" #js {:day "numeric"
                                               :month "long"
                                               :year "numeric"})))
