(ns app.components.text-input
  (:require [clojure.string :as str]
            
            [reagent.core  :as r]
            
            [re-frame.core :refer [dispatch subscribe]]
            
            [reagent-mui.material.text-field      :refer [text-field]]
            [reagent-mui.material.input-adornment :refer [input-adornment]]
            
            [app.components.utils :as utils]))

(defn text-input
  [form-path path & [props]]
  (let [{:keys [label value adornment validators errors] input-type :type} @(subscribe [:zf/node form-path path])
        id (utils/make-id path)]
    [text-field (cond-> (merge
                         {:id          id
                          :label       label
                          :value       value
                          :on-change   #(dispatch [:zf/set-value form-path path (utils/event-value %)])
                          :size        "small"
                          :helper-text " "}
                         props
                         (case input-type
                           (:money :number) {:type "number"}
                           {}))
                  (= :number input-type)
                  (assoc :type "number")

                  adornment
                  (update :InputProps merge {:startAdornment (r/as-element [input-adornment {:position "start"} adornment])})

                  (some #{:required} (keys validators))
                  (assoc :required true)

                  (seq errors)
                  (assoc :error true :helper-text (str/join ", " (vals errors))))]))
