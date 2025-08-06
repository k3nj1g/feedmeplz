(ns app.admin.daily.list.view 
  (:require [reagent.core  :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [tick.core     :as t]

            ["lucide-react" :refer [Calendar ChevronDown Trash2 Edit Plus Utensils ChevronLeft ChevronRight]]

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

(defn pagination
  []
  (let [pagination   @(subscribe [::model/pagination])
        current-page @(subscribe [::model/current-page])]
    (when (and pagination (> (:total-pages pagination) 1))
      [:div.flex.justify-center.items-center.gap-2.mt-6
       ;; Previous button
       [:button.p-2.rounded-lg.border.border-gray-300.hover:bg-gray-50.disabled:opacity-50.disabled:cursor-not-allowed
        {:disabled (= current-page 1)
         :on-click #(when (> current-page 1)
                      (dispatch [model/change-page (dec current-page)]))}
        [:> ChevronLeft {:class "w-4 h-4"}]]
       
       ;; Page numbers
       (for [page (range 1 (inc (:total-pages pagination)))]
         ^{:key page}
         [:button.px-3.py-2.rounded-lg.text-sm.font-medium
          {:class    (if (= current-page page)
                       "bg-blue-500 text-white"
                       "text-gray-700 hover:bg-gray-100")
           :on-click #(dispatch [model/change-page page])}
          page])
       
       ;; Next button
       [:button.p-2.rounded-lg.border.border-gray-300.hover:bg-gray-50.disabled:opacity-50.disabled:cursor-not-allowed
        {:disabled (= current-page (:total-pages pagination))
         :on-click #(when (< current-page (:total-pages pagination))
                      (dispatch [model/change-page (inc current-page)]))}
        [:> ChevronRight {:class "w-4 h-4"}]]
       
       ;; Page info
       [:div.ml-4.text-sm.text-gray-500
        (str "Страница " current-page " из " (:total-pages pagination) 
             " (" (:total-items pagination) " всего)")]])))

(defn daily-menu-view
  [categories {:keys [menu menu-items]}]
  (r/with-let [expanded-menu-id (r/atom nil)]
    (let [menu-disabled (t/< (t/date (:date menu)) (t/date))]
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
  (let [{daily-menus :data} @(subscribe [::model/daily-menus])
        categories  @(subscribe [::model/categories])]
    [:div
     [:div.flex.flex-col.gap-4
      (if (seq daily-menus)
        (for [daily-menu daily-menus]
          ^{:key (get-in daily-menu [:menu :id])}
          [daily-menu-view categories daily-menu])
        [empty-state])]
     [pagination]]))

(defn daily-menu-list
  []
  [:<>
   [header]
   [daily-menus-view]])

(defmethod routes/pages :admin-daily-list [] daily-menu-list)
