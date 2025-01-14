(ns app.admin.catalog.view
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["lucide-react" :refer [Edit Trash2 Plus ChevronDown]]
            [app.components.ui.card :refer [Card CardContent]]
            [app.components.ui.dialog :refer [Dialog DialogContent DialogHeader DialogTitle DialogFooter]]
            [app.components.ui.button :refer [Button]]
            [app.components.ui.input :refer [Input]]
            [app.components.ui.textarea :refer [Textarea]]
            
            [app.routes :as routes]))

(def initial-categories
  ["Салаты" "Супы" "Горячие блюда" "Гарниры" "Десерты"])

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

(defn dish-card [dish on-edit]
  [Card {:class "bg-white"}
   [CardContent {:class "p-4"}
    [:div.flex.justify-between.items-start
     [:div.flex-1
      [:div.flex.justify-between.items-start
       [:div
        [:h3.font-medium.text-lg.mb-1 (:name dish)]
        [:p.text-sm.text-gray-600 (:description dish)]]
       [:div.flex.space-x-2
        [:button.p-2.text-gray-500.hover:text-blue-500
         {:on-click #(on-edit dish)}
         [:> Edit {:class "w-4 h-4"}]]
        [:button.p-2.text-gray-500.hover:text-red-500
         [:> Trash2 {:class "w-4 h-4"}]]]]
      [:div.flex.space-x-4.mt-2.text-sm.text-gray-500
       [:span (str (:price dish) " ₽")]
       [:span (str (:weight dish) " г")]
       [:span (str (:calories dish) " ккал")]]]]]])

(defn edit-dish-dialog [{:keys [dish is-open on-close on-save]}]
  [Dialog {:open is-open :on-open-change on-close}
   [DialogContent {:class "sm:max-w-[500px]"}
    [DialogHeader
     [DialogTitle (if (:id dish) "Редактировать блюдо" "Новое блюдо")]]

    [:div.grid.gap-4.py-4
     [:div.grid.gap-2
      [:label.text-sm.font-medium {:for "name"} "Название"]
      [Input {:id "name"
              :value (:name dish)
              :on-change #(swap! dish assoc :name (.. % -target -value))}]]

     [:div.grid.gap-2
      [:label.text-sm.font-medium {:for "description"} "Описание"]
      [Textarea {:id "description"
                 :value (:description dish)
                 :on-change #(swap! dish assoc :description (.. % -target -value))}]]

     [:div.grid.grid-cols-3.gap-4
      [:div.grid.gap-2
       [:label.text-sm.font-medium {:for "price"} "Цена (₽)"]
       [Input {:id "price"
               :type "number"
               :value (:price dish)
               :on-change #(swap! dish assoc :price (js/Number (.. % -target -value)))}]]

      [:div.grid.gap-2
       [:label.text-sm.font-medium {:for "weight"} "Вес (г)"]
       [Input {:id "weight"
               :type "number"
               :value (:weight dish)
               :on-change #(swap! dish assoc :weight (js/Number (.. % -target -value)))}]]

      [:div.grid.gap-2
       [:label.text-sm.font-medium {:for "calories"} "Калории"]
       [Input {:id "calories"
               :type "number"
               :value (:calories dish)
               :on-change #(swap! dish assoc :calories (js/Number (.. % -target -value)))}]]]]

    [DialogFooter
     [Button {:variant "outline" :on-click on-close} "Отмена"]
     [Button {:on-click #(on-save @dish)} "Сохранить"]]]])

(defn dish-catalog []
  (let [open-category (r/atom "Салаты")
        is-edit-modal-open (r/atom false)
        editing-dish (r/atom nil)]
    (fn []
      [:div.max-w-6xl.mx-auto.px-4.py-6
       [:div.flex.justify-between.items-center.mb-6
        [:h1.text-2xl.font-semibold "Каталог блюд"]
        [Button {:class "flex items-center"
                 :on-click #(do (reset! editing-dish {:name "" :description "" :price 0 :weight 0 :calories 0})
                                (reset! is-edit-modal-open true))}
         [:> Plus {:class "w-4 h-4 mr-2"}]
         "Добавить блюдо"]]

       [:div.flex
        [:div.w-64.pr-6
         [:div.bg-white.rounded-lg.shadow
          (for [category initial-categories]
            ^{:key category}
            [:button.w-full.text-left.px-4.py-3.hover:bg-gray-50
             {:class (if (= @open-category category) "bg-blue-50 text-blue-600" "text-gray-700")
              :on-click #(reset! open-category category)}
             category])]]

        [:div.flex-1.space-y-4
         [:div.flex.justify-between.items-center.mb-4
          [:h2.text-xl.font-medium @open-category]]
         (for [dish (get initial-dishes @open-category)]
           ^{:key (:id dish)}
           [dish-card dish #(do (reset! editing-dish %)
                                (reset! is-edit-modal-open true))])]]

       [edit-dish-dialog
        {:dish @editing-dish
         :is-open @is-edit-modal-open
         :on-close #(reset! is-edit-modal-open false)
         :on-save (fn [updated-dish]
                    (println "Saving dish:" updated-dish)
                    (reset! is-edit-modal-open false)
                    (reset! editing-dish nil))}]])))

(defmethod app.routes/pages :admin-catalog [] [dish-catalog])
