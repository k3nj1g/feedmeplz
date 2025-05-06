(ns app.components.date-picker
  (:require [reagent.core  :as r]
            [re-frame.core :refer [dispatch subscribe]]

            [reagent-mui.material.text-field     :refer [text-field]]
            [reagent-mui.cljs-time-adapter       :refer [cljs-time-adapter]]
            [reagent-mui.x.date-picker           :refer [date-picker] :rename {date-picker mui-date-picker}]
            [reagent-mui.x.localization-provider :refer [localization-provider]]
            
            [app.utils.date :as date-utils])
  (:import (goog.i18n DateTimeSymbols_ru)))

(defn date-picker
  [form-path path {:keys [label disabled min-date max-date format class]
                   :or {format "dd.MM.yyyy"}}]
  (let [value @(subscribe [:zf/get-value form-path path])]
    [localization-provider {:date-adapter   cljs-time-adapter
                            :adapter-locale DateTimeSymbols_ru}
     [mui-date-picker
      (cond-> {:value     (date-utils/parse-date value)
               :on-change (fn [value]
                            (dispatch [:zf/set-value form-path path (date-utils/to-iso-date value)]))
               :inputFormat format
               :render-input (fn [^js params]
                               (let [params' (js->clj params :keywordize-keys true)]
                                 (r/as-element
                                  [text-field
                                   (merge params'
                                          {:class class
                                           :size  "small"})])))}
        label (assoc :label label)
        disabled (assoc :disabled disabled)
        min-date (assoc :minDate (date-utils/parse-date min-date))
        max-date (assoc :maxDate max-date))]]))
