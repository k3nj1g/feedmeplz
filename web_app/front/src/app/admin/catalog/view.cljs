(ns app.admin.catalog.view
  (:require ["lucide-react" :refer [Edit Trash2 Plus UtensilsCrossed]]
            
            [re-frame.core :refer [dispatch subscribe]]

            [reagent-mui.material.card :refer [card]]
            [reagent-mui.material.card-content :refer [card-content]]
            [reagent-mui.material.dialog :refer [dialog]]
            [reagent-mui.material.dialog-actions :refer [dialog-actions]]
            [reagent-mui.material.dialog-content :refer [dialog-content]]
            [reagent-mui.material.dialog-title :refer [dialog-title]]

            [app.components.base :refer [button heading]]
            ;; [app.components.ui.card :refer [Card CardContent]]
            ;; [app.components.ui.dialog :refer [Dialog DialogContent DialogHeader DialogTitle DialogFooter]]
            ;; [app.components.ui.button :refer [Button]]
            ;; [app.components.ui.input :refer [Input]]
            ;; [app.components.ui.textarea :refer [Textarea]]
            
            [app.routes :as routes]

            [app.components.text-input   :refer [text-input]]
            
            [app.admin.catalog.form  :as form]
            [app.admin.catalog.model :as model]))

(def initial-dishes
  {"Салаты" [{:id 1
              :name "Цезарь с курицей"
              :description "Салат романо, куриное филе, гренки, пармезан, соус цезарь"
              :price 420
              :weight 220
              :calories 350}
             {:id 2
              :name "Греческий"
              :description "Свежие овощи, маслины, сыр фета, оливковое масло"
              :price 380
              :weight 200
              :calories 280}]
   "Супы" [{:id 3
            :name "Борщ"
            :description "Традиционный борщ со сметаной"
            :price 320
            :weight 300
            :calories 240}
           {:id 4
            :name "Куриный суп"
            :description "Куриный бульон с лапшой и овощами"
            :price 280
            :weight 300
            :calories 180}]})

(defn add-dish-button
  []
  [button
   {:type     "primary"
    :on-click #(do (dispatch [:toggle-dialog-menu :add-dish])
                   (dispatch [:zf/init form/form-path form/form-schema]))}
   [:> Plus {:class "w-4 h-4 mr-2"}]
   "Добавить блюдо"])

(defn empty-state
  []
  [:div.flex.flex-col.items-center.justify-center.p-8.bg-gray-50.rounded-lg.border-2.border-dashed.border-gray-200
   [:> UtensilsCrossed {:class "w-12 h-12 text-gray-400 mb-4"}]
   [:h3.text-lg.font-medium.text-gray-900.mb-1 "В этой категории пока нет блюд"]
   [:p.text-sm.text-gray-500.mb-4.text-center
    "Добавьте новые блюда, нажав на кнопку \"Добавить блюдо\" вверху страницы"]
   [add-dish-button]])

(defn dish-card
  [dish]
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
         #_{:on-click #(on-edit dish)}
         [:> Edit {:class [:w-4 :h-4]}]]
        [:button.p-2.text-gray-500.hover:text-red-500
         [:> Trash2 {:class [:w-4 :h-4]}]]]]
      [:div.flex.space-x-4.mt-2.text-sm.text-gray-500
       [:span (str (:price dish) " ₽")]
       [:span (str (:weight dish) " г")]
       [:span (str (:calories dish) " ккал")]]]]]])

(defn edit-dish-dialog
  [{:keys [dish]}]
  (let [is-open  @(subscribe [:active-dialog :add-dish])
        on-close #(dispatch [:close-dialog-menu :add-dish])]
    [dialog
     {:open       is-open
      :full-width true
      :max-width  "sm"}
     [dialog-title
      {:class "!pb-0"}
      (if (:id dish) "Редактировать блюдо" "Новое блюдо")]
     [:div]
     [dialog-content
      [:div.grid.gap-1
       [text-input form/form-path [:name]]
       [text-input form/form-path [:description]]
       [:div.grid.grid-cols-3.gap-4
        [text-input form/form-path [:price]]
        [text-input form/form-path [:weight]]
        [text-input form/form-path [:calories]]]]]

      [:div
       {:class ["px-4" "pb-4"]}
       [dialog-actions
        [button {:type "default" :on-click on-close} "Отмена"]
        [button {:type "primary" :on-click #(dispatch [:eval-form form/form-path])} "Сохранить"]]]]))

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
      (for [category categories]
        ^{:key (:id category)}
        [:button.w-full.text-left.px-4.py-3.hover:bg-gray-50
         {:class    (if (= (:id active-category) (:id category))
                      ["bg-blue-50" "text-blue-600"]
                      "text-gray-700")
          :on-click #(dispatch [:db/set [:page :active-category] category])}
         (:name category)])]]))

(defn category-content
  [active-category]
  (let [dishes (if (= 1 (:id active-category))
                 [{:id 1
                   :name "Цезарь с курицей"
                   :description "Салат романо, куриное филе, гренки, пармезан, соус цезарь"
                   :price 420
                   :weight 220
                   :calories 350}
                  {:id 2
                   :name "Греческий"
                   :description "Свежие овощи, маслины, сыр фета, оливковое масло"
                   :price 380
                   :weight 200
                   :calories 280}]
                 @(subscribe [::model/dishes-by-category active-category]))]
    [:div.flex-1.space-y-4
     (if (empty? dishes)
       [empty-state]
       [:<>
        [:div.flex.justify-between.items-center.mb-4
         [:h2.text-xl.font-medium (:name active-category)]]
        (for [dish dishes]
          ^{:key (:id dish)}
          [dish-card dish])])]))

(defn dish-catalog
  []
  (let [{:keys [active-category]} @(subscribe [::model/data])]
    [:<>
     [header]

     [:div.flex
      [vertical-tab active-category]
      [category-content active-category]]

     [edit-dish-dialog
      {}]]))

(defmethod app.routes/pages :admin-catalog [] dish-catalog)
