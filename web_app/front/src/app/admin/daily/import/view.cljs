(ns app.admin.daily.import.view
  (:require ["lucide-react" :refer [Check X AlertCircle Loader2 CheckCircle]]

            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as reagent]

            [app.components.base :refer [button]]
            [app.components.file-input :refer [file-input]]
            [app.components.card-parts :refer [card]]

            ;; [reagent-mui.material.card :refer [card]]
            [reagent-mui.material.typography :refer [typography]]
            [reagent-mui.material.box :refer [box]]
            [reagent-mui.material.stack :refer [stack]]
            [reagent-mui.material.alert :refer [alert]]
            [reagent-mui.material.button :refer [button] :rename {button mui-button}]

            [app.utils.date :as date-utils]
            [app.routes :as routes]

            [app.admin.daily.import.model :as model]
            [app.admin.daily.import.form  :as form]))

(defn file-input-section
  []
  (let [status            @(subscribe [:admin-daily-import/status])
        is-disabled       (boolean (#{:validating :importing} status))
        verified?         (boolean (#{:validated :importing} status))
        validation-result @(subscribe [:admin-daily-import/validation-result])]
    [box
     {:sx {:backgroundColor "#eff6ff"
           :borderRadius    "8px"
           :padding         "24px"
           :margin-bottom   "24px"}}
     [typography
      {:variant "body1"
       :sx      {:color         "#374151"
                 :margin-bottom "16px"
                 :font-weight   500}}
      "Выберите файл с меню"]
     [typography
      {:variant "body2"
       :sx      {:color         "#4b5563"
                 :line-height   "1.6"
                 :margin-bottom "16px"}}
      "Загрузите Excel-файл (.xlsx или .xls) с меню на день. Файл должен содержать информацию о блюдах: категорию, название, цену, калории и вес."]
     [file-input form/form-path [:file]
      {:accept      ".xlsx,.xls"
       :button-text "Загрузить файлы"
       :verified?   (and verified? validation-result)
       :props       {:disabled is-disabled}}]]))

(defn validation-button
  []
  (let [{:keys [file status validation-result]} @(subscribe [::model/validate-button])]
    (when (and file (not validation-result))
      [button
       {:type      "default"
        :fullWidth true
        :disabled  (or (not file) (#{:validating :importing} status))
        :on-click  #(dispatch [:admin-daily-import/validate-file file])}
       (when (= status :validating)
         [:> Loader2 {:class "w-4 h-4 mr-2 animate-spin"}])
       (if (= status :validating)
         "Проверка..."
         "Проверить файл")])))

(defn error-message
  []
  (when-let [error @(subscribe [:admin-daily-import/error])]
    [:div.mb-6.p-4.bg-red-50.border.border-red-200.rounded-lg.flex.items-start
     [:> AlertCircle {:class "w-5 h-5 mr-3 text-red-600 flex-shrink-0 mt-0.5"}]
     [:div
      [:h4.font-medium.text-red-900.mb-1 "Ошибка"]
      [:p.text-sm.text-red-700 error]]]))

(defn validation-errors-table
  [errors]
  (when (seq errors)
    [:div.mb-6
     [:h3.text-lg.font-medium.mb-3.text-red-900 "Ошибки валидации"]
     [:div.overflow-x-auto
      [:table.min-w-full.divide-y.divide-gray-200.border.border-gray-200.rounded-lg
       [:thead.bg-red-50
        [:tr
         [:th.px-4.py-3.text-left.text-xs.font-medium.text-red-900.uppercase "Строка"]
         [:th.px-4.py-3.text-left.text-xs.font-medium.text-red-900.uppercase "Поле"]
         [:th.px-4.py-3.text-left.text-xs.font-medium.text-red-900.uppercase "Сообщение"]]]
       [:tbody.bg-white.divide-y.divide-gray-200
        (for [[idx error] (map-indexed vector errors)]
          ^{:key idx}
          [:tr
           [:td.px-4.py-3.text-sm (or (:row error) "-")]
           [:td.px-4.py-3.text-sm (:field error)]
           [:td.px-4.py-3.text-sm.text-red-700 (:message error)]])]]]]))

(defn dishes-table
  [title dishes color-class]
  (when (seq dishes)
    [:div.mb-6
     [:h3.text-lg.font-medium.mb-3 title]
     [:div.overflow-x-auto
      [:table.min-w-full.divide-y.divide-gray-200.border.border-gray-200.rounded-lg
       [:thead {:class (str "bg-" color-class "-50")}
        [:tr
         [:th.px-4.py-3.text-left.text-xs.font-medium.text-gray-700.uppercase "Категория"]
         [:th.px-4.py-3.text-left.text-xs.font-medium.text-gray-700.uppercase "Название"]
         [:th.px-4.py-3.text-left.text-xs.font-medium.text-gray-700.uppercase.text-right "Цена"]
         [:th.px-4.py-3.text-left.text-xs.font-medium.text-gray-700.uppercase.text-right "Ккал"]
         [:th.px-4.py-3.text-left.text-xs.font-medium.text-gray-700.uppercase "Вес"]]]
       [:tbody.bg-white.divide-y.divide-gray-200
        (for [[idx dish] (map-indexed vector dishes)]
          ^{:key idx}
          [:tr
           [:td.px-4.py-3.text-sm (:category dish)]
           [:td.px-4.py-3.text-sm.font-medium (:name dish)]
           [:td.px-4.py-3.text-sm.text-right (str (:price dish) " ₽")]
           [:td.px-4.py-3.text-sm.text-right (or (:kcals dish) "-")]
           [:td.px-4.py-3.text-sm (or (:weight dish) "-")]])]]]]))

(defn validation-summary
  []
  (when-let [result @(subscribe [:admin-daily-import/validation-result])]
    (let [{:keys [status date summary existing-dishes new-dishes errors existing-menu?]} result]
      [:div.mb-6
       [card
        [:div.p-6
         ;; Status badge
         [:div.mb-4
          (if (= status :valid)
            [:div.inline-flex.items-center.px-3.py-1.rounded-full.bg-green-100.text-green-800
             [:> Check {:class "w-4 h-4 mr-2"}]
             [:span.font-medium "Файл валиден"]]
            [:div.inline-flex.items-center.px-3.py-1.rounded-full.bg-red-100.text-red-800
             [:> X {:class "w-4 h-4 mr-2"}]
             [:span.font-medium "Найдены ошибки"]])]

         ;; Date
         [:div.mb-4
          [:h3.text-lg.font-medium.mb-2 "Дата меню"]
          [:p.text-gray-700 (date-utils/->ru-verbose date)]
          (when existing-menu?
            [:p.text-sm.text-red-600.mt-1 "⚠ Меню на эту дату уже существует!"])]

         ;; Summary
         [:div.mb-6
          [:h3.text-lg.font-medium.mb-3 "Сводка"]
          [:div.grid.grid-cols-2.md:grid-cols-4.gap-4
           [:div.bg-gray-50.p-3.rounded-lg
            [:p.text-sm.text-gray-600 "Всего блюд"]
            [:p.text-2xl.font-bold.text-gray-900 (:total-dishes summary)]]
           [:div.bg-green-50.p-3.rounded-lg
            [:p.text-sm.text-gray-600 "Найдено"]
            [:p.text-2xl.font-bold.text-green-600 (:existing-dishes summary)]]
           [:div.bg-yellow-50.p-3.rounded-lg
            [:p.text-sm.text-gray-600 "Новых"]
            [:p.text-2xl.font-bold.text-yellow-600 (:new-dishes summary)]]
           [:div.bg-red-50.p-3.rounded-lg
            [:p.text-sm.text-gray-600 "Ошибки"]
            [:p.text-2xl.font-bold.text-red-600 (:invalid-dishes summary)]]]]

         ;; Errors table
         (when (seq errors)
           [validation-errors-table errors])

         ;; Existing dishes
         (when (seq existing-dishes)
           [dishes-table "Найденные блюда (будут добавлены в меню)" existing-dishes "green"])

         ;; New dishes
         (when (seq new-dishes)
           [:div
            [dishes-table "Новые блюда (будут созданы в каталоге)" new-dishes "yellow"]
            [:div.mt-4.flex.items-center.gap-3
             [:label.flex.items-center.cursor-pointer
              [:input
               {:type "checkbox"
                :checked @(subscribe [:admin-daily-import/create-new-dishes?])
                :on-change #(dispatch [:admin-daily-import/toggle-create-new-dishes])
                :class "w-4 h-4 text-blue-600 rounded focus:ring-blue-500"}]
              [:span.ml-2.text-sm.text-gray-700 "Автоматически создать новые блюда в каталоге"]]]])]]])))

(defn import-button
  []
  (let [can-import? @(subscribe [:admin-daily-import/can-import?])
        status @(subscribe [:admin-daily-import/status])
        validation-result @(subscribe [:admin-daily-import/validation-result])]
    (when validation-result
      [:div.mb-6
       [button
        {:type "primary"
         :disabled (not can-import?)
         :on-click #(dispatch [:admin-daily-import/execute])}
        (when (= status :importing)
          [:> Loader2 {:class "w-4 h-4 mr-2 animate-spin"}])
        (if (= status :importing)
          "Импорт..."
          "Импортировать меню")]])))

(defn success-message
  []
  (when-let [result @(subscribe [:admin-daily-import/import-result])]
    [:div.mb-6.p-4.bg-green-50.border.border-green-200.rounded-lg.flex.items-start
     [:> Check {:class "w-5 h-5 mr-3 text-green-600 flex-shrink-0 mt-0.5"}]
     [:div
      [:h4.font-medium.text-green-900.mb-1 "Импорт успешно выполнен!"]
      [:p.text-sm.text-green-700
       (str "Создано блюд: " (:dishes-created result) ", "
            "Добавлено позиций в меню: " (:menu-items-created result))]
      [:div.mt-3
       [button
        {:type "secondary"
         :on-click #(dispatch [:navigate :admin-daily-list])}
        "Перейти к списку меню"]]]]))

(defn verification-success-alert
  []
  (let [validation-result @(subscribe [:admin-daily-import/validation-result])
        status @(subscribe [:admin-daily-import/status])]
       (when (and validation-result (= (:status validation-result) :valid) (= status :validated))
      [alert
       {:severity "success"
        :icon     (reagent/as-element [:> CheckCircle {:class "w-5 h-5"}])
        :sx       {:border-radius "8px"
                   :margin-bottom "24px"}}
       [typography
        {:variant "body2"}
        "Файл успешно проверен и готов к импорту!"]])))

(defn import-view
  []
  (let [status @(subscribe [:admin-daily-import/status])
        file @(subscribe [::model/import-file])]
    [:div.min-h-screen.p-4
     [typography
      {:variant "h4"}
      "Импорт меню из Excel"]
     [:div.mt-3
      (when (= status :success)
        [success-message])

      (when-not (= status :success)
        [stack
         {:spacing 3}
         [file-input-section]
         [validation-button]
         [verification-success-alert]
         [error-message]
         [validation-summary]
         [import-button]
         
         (when file
           [mui-button
            {:variant  "text"
             :size     "small"
             :on-click #(dispatch [:admin-daily-import/reset])
             :sx       {:color          "#6b7280"
                        :text-transform "none"
                        :align-self     "flex-start"
                        "&:hover"       {:background-color "#f3f4f6"}}}
            "Сбросить"])])]]))

(defmethod routes/pages :admin-daily-import [] import-view)
