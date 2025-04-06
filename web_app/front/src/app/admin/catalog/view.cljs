(ns app.admin.catalog.view
  (:require ["lucide-react" :refer [Edit Save Trash2 Plus Search UtensilsCrossed]]
            
            [re-frame.core :refer [dispatch subscribe]]

            [reagent-mui.material.card :refer [card]]
            [reagent-mui.material.card-content :refer [card-content]]
            [reagent-mui.material.dialog :refer [dialog]]
            [reagent-mui.material.dialog-actions :refer [dialog-actions]]
            [reagent-mui.material.dialog-content :refer [dialog-content]]
            [reagent-mui.material.dialog-title :refer [dialog-title]]

            [app.components.base :refer [button heading]]
            
            [app.routes :as routes]

            [app.components.text-input   :refer [text-input]]
            
            [app.admin.catalog.form  :as form]
            [app.admin.catalog.model :as model]))

(defn add-dish-button
  []
  [button
   {:type     "primary"
    :on-click #(do (dispatch [:open-dialog :edit-dish])
                   (dispatch [:zf/init form/form-path-update form/form-schema-update]))}
   [:> Plus {:class "w-4 h-4 mr-2"}]
   "Добавить блюдо"])

(defn empty-state
  []
  [:div.flex.flex-col.items-center.justify-center.p-8.bg-gray-50.rounded-lg.border-2.border-dashed.border-gray-200
   [:> UtensilsCrossed {:class "w-12 h-12 text-gray-400 mb-4"}]
   [:h3.text-lg.font-medium.text-gray-900.mb-1 "В этой категории пока нет блюд"]
   [:p.text-sm.text-gray-500.mb-4.text-center
    "Добавьте новые блюда, нажав на кнопку \"Добавить блюдо\" вверху страницы"]])

(defn dish-card
  [{:keys [on-edit on-delete] :as dish}]
  [card
   [card-content
    [:div.flex.justify-between.items-start
     [:div.flex-1
      [:div.flex.justify-between.items-start
       [:div
        [:h3.font-medium.text-lg.mb-1 (:name dish)]
        [:p.text-sm.text-gray-600 (:description dish)]]
       [:div.flex.space-x-2
        [:button.p-2.text-gray-500.hover:text-blue-500
         {:on-click on-edit}
         [:> Edit {:class ["w-5" "h-5" "text-blue-500"]}]]
        [:button.p-2.text-gray-500.hover:text-red-500
         {:on-click on-delete}
         [:> Trash2 {:class ["w-5" "h-5" "text-red-500"]}]]]]
      [:div.flex.space-x-4.mt-2.text-sm.text-gray-500
       [:span (str (:price dish) " ₽")]
       [:span (str (:weight dish) " г")]
       [:span (str (:kcals dish) " ккал")]]]]]])

(defn edit-dish-dialog
  []
  (let [{:keys [open]}         @(subscribe [:dialog-state :edit-dish])
        on-close               #(dispatch [:close-dialog :edit-dish])
        {:keys [dish on-save]} @(subscribe [::model/edit-dish-dialog-data])]
    [dialog
     {:open       (boolean open)
      :full-width true
      :max-width  "sm"}
     [dialog-title
      {:class "!pb-0"}
      (if (:id dish) "Редактировать блюдо" "Новое блюдо")]
     [dialog-content
      [:form.pt-5
       {:on-key-down (fn [event]
                       (when (= (.-key event) "Enter")
                         (.preventDefault event)
                         (on-save)))}
       [:div.grid.gap-4
        [text-input form/form-path-update [:name]]
        [:div.grid.grid-cols-3.gap-4
         [text-input form/form-path-update [:price]
          {:adornment "₽"}]
         [text-input form/form-path-update [:weight]
          {:adornment "г"}]
         [text-input form/form-path-update [:kcals]
          {:adornment "ккал"}]]]]]

      [:div
       {:class ["px-4" "pb-4"]}
       [dialog-actions
        [button {:on-click on-close} "Отмена"]
        [button {:type "success" :on-click on-save} 
         [:> Save {:class "w-4 h-4 mr-2"}]
         "Сохранить"]]]]))

(defn delete-dish-dialog
  []
  (let [{:keys [open]} @(subscribe [:dialog-state :delete-dish])
        on-close #(dispatch [:close-dialog :delete-dish])
        {:keys [dish on-delete]} @(subscribe [::model/delete-dish-dialog-data])]
    [dialog
     {:open     (boolean open)
      :on-close on-close}
     [dialog-title "Удалить блюдо"]
     [dialog-content
      [:p (str "Вы уверены, что хотите удалить блюдо \"" (:name dish) "\"?")]]
     [:div
      {:class ["px-4" "pb-4"]}
      [dialog-actions
       [button {:type "default" :on-click on-close} "Отмена"]
       [button {:type "danger"  :on-click on-delete} 
        [:> Trash2 {:class "w-4 h-4 mr-2"}]
        "Удалить"]]]]))

(defn header
  []
  [:div.flex.justify-between.items-center.mb-6
   [heading "Каталог блюд"]
   [add-dish-button]])

(defn vertical-tab
  [active-category]
  (let [categories @(subscribe [::model/categories])]
    [:div.w-64.pr-6
     [:div.bg-white.rounded-lg.shadow
      (doall
       (for [category categories]
         ^{:key (:id category)}
         [:button.w-full.text-left.px-4.py-3.hover:bg-gray-50
          {:class    (if (= (:id active-category) (:id category))
                       ["bg-blue-50" "text-blue-600"]
                       "text-gray-700")
           :on-click (partial model/change-category category)}
          (:name category)]))]]))

(defn category-content
  [active-category]
  (let [{:keys [dishes no-items?]} 
        @(subscribe [::model/dishes-by-category active-category])]
    [:div.flex-1.space-y-4
     (if no-items?
       [empty-state]
       [:<>
        [:div.flex.justify-between.items-center.mb-4
         [:h2.text-xl.font-medium (:name active-category)]
         [text-input form/form-path-search [:search]
          {:adornment [:> Search {:class "w-5 h-5 text-gray-400 mr-2"}]
           :props     {:placeholder "Поиск блюд..."}}]]
        (if (empty? dishes)
          [:div.p-8.bg-gray-50.rounded-lg.border-2.border-dashed.border-gray-200
           [:p.text-sm.text-gray-500.text-center
            "По вашему запросу ничего не найдено"]]
          (for [dish dishes]
            ^{:key (:id dish)}
            [dish-card dish]))])]))

(defn dish-catalog
  []
  (let [{:keys [active-category]} @(subscribe [::model/data])]
    [:<>
     [header]

     [:div.flex
      [vertical-tab active-category]
      [category-content active-category]]

     [edit-dish-dialog]
     [delete-dish-dialog]]))

(defmethod app.routes/pages :admin-catalog [] dish-catalog)
