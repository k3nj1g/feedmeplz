(ns app.components.date-picker
  (:require [reagent.core :as r]
            
            [reagent-mui.material.text-field     :refer [text-field]]
            [reagent-mui.x.date-picker           :refer [date-picker]]
            [reagent-mui.x.localization-provider :refer [localization-provider]]
            [reagent-mui.x.adapter-date-fns      :refer [adapter-date-fns]]
            
            [date-fns-ru :as date-fns-ru]))

(defn date-picker
  "Компонент выбора даты с русской локализацией.
   
   Параметры:
   - value: текущее значение даты (объект Date или строка в формате ISO)
   - on-change: функция, вызываемая при изменении даты
   - label: метка поля (опционально)
   - disabled: флаг отключения компонента (опционально)
   - min-date: минимальная доступная дата (опционально)
   - max-date: максимальная доступная дата (опционально)
   - format: формат отображения даты (опционально, по умолчанию 'dd.MM.yyyy')
   - class: дополнительные CSS классы (опционально)
   
   Пример использования:
   [date-picker {:value (js/Date.)
                 :on-change #(js/console.log %)
                 :label \"Выберите дату\"}]"
  [{:keys [value on-change label disabled min-date max-date format class]
    :or {format "dd.MM.yyyy"}}]
  (let [date-value (if (string? value)
                     (js/Date. value)
                     value)]
    [localization-provider {:date-adapter adapter-date-fns
                            :adapter-locale date-fns-ru/ru}
     [date-picker
      (cond-> {:value date-value
               :input-format format
               :onChange on-change
               :renderInput (fn [params]
                              (r/as-element
                               [text-field
                                (merge
                                 (js->clj params :keywordize-keys true)
                                 {:fullWidth true
                                  :variant "outlined"
                                  :class class})]))}
        label (assoc :label label)
        disabled (assoc :disabled disabled)
        min-date (assoc :minDate min-date)
        max-date (assoc :maxDate max-date))]]))
