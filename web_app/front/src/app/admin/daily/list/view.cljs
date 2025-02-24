(ns app.admin.daily.list.view 
  (:require [reagent.core  :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [tick.core     :as t]

            ["lucide-react" :refer [Calendar ChevronDown Trash2 Edit Plus Utensils]]

            [app.components.base :refer [button heading]]
            [app.components.card-parts :refer [card]]

            [app.routes :as routes]

            [app.utils.date :as date-utils]

            [app.admin.daily.list.model :as model]))

(defn add-menu-button
  []
  [button
   {:type     "primary"
    :on-click #(dispatch [:navigate :admin-daily-create])}
   [:> Plus {:class "w-4 h-4 mr-2"}]
   "Создать меню"])

(defn header
  []
  [:div.flex.justify-between.items-center.mb-6
   [heading "Список меню"]
   [add-menu-button]])

(defn empty-state
  []
  [:div.flex.flex-col.items-center.justify-center.p-8.bg-gray-50.rounded-lg.border-2.border-dashed.border-gray-200
   [:> Utensils {:class "w-12 h-12 text-gray-400 mb-4"}]
   [:h3.text-lg.font-medium.text-gray-900.mb-1 "Меню пока не добавлено"]
   [:p.text-sm.text-gray-500.mb-4.text-center
    "Добавьте новые блюда, нажав на кнопку \"Создать меню\" вверху страницы"]])    

(defn daily-menu-items-view
  [category items]
  [:div.mb-4.last:mb-0
   [:h4.font-medium.mb-2 (:name category)]
   [:div.grid.grid-cols-1.md:grid-cols-2.gap-2
    (for [item items]
      ^{:key (:id item)}
      [:div.p-3.bg-gray-50.rounded-lg
       [:div.flex.justify-between.items-center
        [:span (:name item)]
        [:span.text-gray-600 (str (:price item) " ₽")]]])]])

(defn daily-menu-view
  [categories {:keys [menu menu-items]}]
  (r/with-let [expanded-menu-id (r/atom nil)]
    (let [menu-disabled (t/< (t/date (t/instant (:date menu))) (t/date))]
      [card
       [:div.p-4.flex.items-center.justify-between
        [:div.flex.items-center.space-x-4
         [:> Calendar {:class ["w-5" "h-5" "text-gray-500"]}]
         [:div
          [:h3.font-medium (date-utils/->ru-verbose (:date menu))]
          [:p.text-sm.text-gray-500
           (str "Блюд: " (count menu-items))]]]
        [:div.flex.items-center.space-x-2
         [:button.p-2.hover:bg-gray-100.rounded-full
          {:on-click #(swap! expanded-menu-id (fn [current-id]
                                                (if (= current-id (:id menu))
                                                  nil
                                                  (:id menu))))}
          [:> ChevronDown {:class ["w-5" "h-5" "transform" "transition-transform"
                                   (when (= @expanded-menu-id (:id menu)) "rotate-180")]}]]
         [:button.p-2.hover:bg-gray-100.rounded-full
          (cond-> {:on-click #(dispatch [:navigate :admin-daily-update {:id (:id menu)}])
                   :disabled menu-disabled}
            menu-disabled
            (assoc :class "cursor-not-allowed"))
          [:> Edit
           {:class ["w-5" "h-5" (if menu-disabled "text-gray-300" "text-blue-500")]}]]
         [:button.p-2.hover:bg-gray-100.rounded-full
          [:> Trash2 {:class ["w-5" "h-5" "text-red-500"]}]]]]
       (when (= @expanded-menu-id (:id menu))
         [:div.border-t.p-4
          (for [category categories
                :when (seq (filter #(= (:id category) (:category_id %)) menu-items))]
            ^{:key category}
            [daily-menu-items-view category (filter #(= (:id category) (:category_id %)) menu-items)])])])))

(defn daily-menus-view
  []
  (let [daily-menus @(subscribe [::model/daily-menus])
        categories  @(subscribe [::model/categories])]
    [:div.flex.flex-col.gap-4
     (if (seq daily-menus)
       (for [daily-menu daily-menus]
         ^{:key (get-in daily-menu [:menu :id])}
         [daily-menu-view categories daily-menu])
       [empty-state])]))

(defn daily-menu-list []
  [:<>
   [header]
   [daily-menus-view]])

(defmethod routes/pages :admin-daily-list [] daily-menu-list)
