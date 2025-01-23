(ns app.admin.daily.view
  (:require ["lucide-react" :refer [Plus X Save ChevronDown Search List]]
            [re-frame.core :refer [dispatch subscribe]]

            [reagent.core :as r]

            [app.components.base :refer [button heading]]
            [app.components.card-parts :refer [card card-header card-content subcard subcard-header]]

            [app.routes :as routes]

            [app.admin.daily.model :as model]))

(defn header
  [show-selected]
  [:div.flex.justify-between.items-center.mb-6
   [heading "Меню дня"]
   [button
    {:type     "primary"
     :on-click #(swap! show-selected not)}
    [:> List {:class "w-4 h-4 mr-2"}]
    (if @show-selected "Скрыть выбранное" "Показать выбранное")]])

(defn chosen-dishes
  []
  [card
   {:class "mb-6"}
   [card-header
    {}
    "Выбранные блюда"]])

(defn dishes-by-category
  [active-category category]
  (let [dishes @(subscribe [::model/dishes-by-category category])]
    [subcard
     [subcard-header
      {}
      (:name category)
      [:div.flex.items-center
       [:span.mr-2.text-sm.text-gray-500 "Выбрано: 0"]
       [:> ChevronDown
        {:class (cond-> ["w-5" "h-5" "transform" "transition-transform"]
                  (= @active-category category)
                  (conj "rotate-180"))}]]]]))

(defn dishes-by-categories
  [active-category]
  (let [categories @(subscribe [::model/categories])]
    [card
     [card-header
      "Формирование меню на день"]
     [card-content
      [:div.flex.flex-col.gap-4
       (for [category categories]
         ^{:key (:id category)}
         [dishes-by-category active-category category])]]]))

(defn menu-day-management
  []
  (let [show-selected   (r/atom false)
        active-category (r/atom nil)]
    (fn []
      [:<>
       [header show-selected]
       (when @show-selected
         [chosen-dishes])
       [dishes-by-categories active-category]])))

(defmethod app.routes/pages :admin-daily [] menu-day-management)
