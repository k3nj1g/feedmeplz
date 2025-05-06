(ns app.utils.date
  (:require  [cljs-time.format :as tf]
             [cljs-time.coerce :as tc]))

(defn ->ru-verbose
  "Форматирует дату в виде строки 'день месяц год' на русском языке.
   Принимает объект Date или строку в формате ISO 8601 (YYYY-MM-DD).
   Возвращает отформатированную строку."
  [date]
  (let [date-obj (if (string? date)
                   (js/Date. date)
                   date)]
    (.toLocaleDateString date-obj "ru-RU" #js {:day "numeric"
                                               :month "long"
                                               :year "numeric"})))

(defn parse-date
  [date]
  (when date
    (if (string? date)
      (tc/to-date-time (tf/parse date))
      date)))

(defn to-iso-date
  [date]
  (when date
    (tf/unparse (tf/formatters :date) date)))
