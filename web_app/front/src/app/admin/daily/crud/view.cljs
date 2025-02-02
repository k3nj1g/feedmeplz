(ns app.admin.daily.crud.view
  (:require ["lucide-react" :refer [Calendar X Save ChevronDown Search List]]
            [re-frame.core :refer [dispatch subscribe]]

            [reagent.core :as r]

            [app.components.base       :refer [button heading]]
            [app.components.card-parts :refer [card card-header card-content]]
            [app.components.text-input :refer [text-input]]

            [app.routes :as routes]

            [app.helpers    :as h]
            [app.utils.date :as date-utils]

            [app.admin.daily.crud.form  :as form]
            [app.admin.daily.crud.model :as model])

  (:require-macros [ps]))

(def items-per-page 10)

(defn set-value
  [category dish]
  (dispatch [:zf/set-value form/form-path [(form/category->path category) :dishes] dish]))

(defn add-dish
  [category selected-items item]
  (set-value category (vec (conj (or selected-items []) item))))

(defn remove-dish
  [category selected-items item]
  (set-value category (remove #(= (:id item) (:id %)) selected-items)))

(defn header
  [show-selected]
  [:div.flex.justify-between.items-center.mb-6
   [heading [:div.flex.gap-2
             "Меню дня"
             [:div.flex.items-center.text-base.border.rounded-lg.border-gray-200.px-2.gap-1.text
              [:> Calendar {:class "w-4 h-4"}]
              (date-utils/->ru-verbose (js/Date.))]]]
   [button
    {:type     "primary"
     :on-click #(swap! show-selected not)}
    [:> List {:class "w-4 h-4 mr-2"}]
    (if @show-selected "Скрыть выбранное" "Показать выбранное")]])

(defn selected-dishes-by-category
  [category]
  (let [selected-items @(subscribe [::model/selected-dishes-by-category category])]
    (when (seq selected-items)
      [:div.mb-4
       [:h3.font-medium.mb-2 (:name category)]
       [:div.grid.grid-cols-1.md:grid-cols-2.gap-2
        (for [item (sort-by :name selected-items)]
          ^{:key (:id item)}
          [:div.p-3.bg-blue-50.rounded-lg.border.border-blue-200
           [:div.flex.justify-between.items-center
            [:span (:name item)]
            [:div.flex.items-center
             [:span.text-gray-600.mr-2 (str (:price item) " ₽")]
             [:button.text-red-500.hover:text-red-700
              {:on-click #(remove-dish category selected-items item)}
              [:> X {:class "w-4 h-4"}]]]]])]])))

(defn selected-dishes
  []
  (let [categories @(subscribe [::model/categories])]
    [card
     {:class "mb-6"}
     [card-header
      {}
      "Выбранные блюда"]
     [card-content
      (for [category categories]
        ^{:key (str "selected-" (:id category))}
        [selected-dishes-by-category category])]]))

(defn pages
  [active-page items]
  [:div.mt-4.flex.justify-center.gap-2
   (let [total-pages (Math/ceil (/ (count items) items-per-page))]
     (doall
      (for [page (range 1 (inc total-pages))]
        ^{:key page}
        [:button.px-3.py-1.rounded
         {:class    (if (= @active-page page)
                      "bg-blue-500 text-white"
                      "bg-white text-gray-600")
          :on-click #(reset! active-page page)}
         page])))])

(defn dishes-by-category
  [_active-category _category]
  (let [active-page (r/atom 1)]
    (fn [active-category category]
      (let [dishes         @(subscribe [::model/dishes-by-category category])
            selected-items @(subscribe [:zf/get-value form/form-path [(form/category->path category) :dishes]])]
        [:div
         [:button.w-full.bg-white.border.rounded-lg.shadow-sm
          {:on-click #(reset! active-category (if (= @active-category category) nil category))}
          [:div.flex.justify-between.items-center.p-4
           [:h3.font-semibold.leading-none.tracking-tight
            {:on-click #(reset! active-category (if (= % category) nil category))}
            (:name category)]
           [:div.flex.items-center
            [:span.mr-2.text-sm.text-gray-500
             (str "Выбрано: " (count selected-items))]
            [:> ChevronDown
             {:class (cond-> ["w-5" "h-5" "transform" "transition-transform"]
                       (= @active-category category)
                       (conj "rotate-180"))}]]]]
         (when (= @active-category category)
           [:div.mt-2.p-4.bg-gray-50.rounded-lg
            [text-input form/form-path [(form/category->path category) :search]
             {:adornment [:> Search {:class "w-5 h-5 text-gray-400 mr-2"}]
              :props     {:full-width  true
                          :placeholder "Поиск блюд..."}}]
            [:div.grid.grid-cols-1.md:grid-cols-2.gap-2
             (let [paginated-items (->> dishes
                                        (drop (* (dec @active-page) items-per-page))
                                        (take items-per-page))]
               (for [item paginated-items]
                 ^{:key (:id item)}
                 [:div.p-3.rounded-lg.cursor-pointer.border
                  (if (h/in? (:id item) (map :id selected-items))
                    {:class    "bg-blue-100 border-blue-300"
                     :on-click #(remove-dish category selected-items item)}
                    {:class    "bg-white border-gray-200"
                     :on-click #(add-dish category selected-items item)})
                  [:div.flex.justify-between.items-center
                   [:span (:name item)]
                   [:span.text-gray-600 (str (:price item) " ₽")]]]))]
            [pages active-page dishes]])]))))

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

(defn footer
  []
  (let [selected-items-count @(subscribe [::model/selected-items-count])
        {:keys [save]}       @(subscribe [::model/buttons])]
    (ps/persist-scope)
    [:div.fixed.bottom-0.left-0.right-0.bg-white.shadow-lg.border-t
     [:div.max-w-6xl.mx-auto.px-4.py-4.flex.justify-between.items-center
      [:div
       [:span.text-gray-600 "Выбрано блюд:"]
       [:span.ml-2 selected-items-count]]
      [button
       (assoc save :type "success")
       [:> Save {:class "w-4 h-4 mr-2"}]
       "Сохранить меню"]]]))

(defn menu-day-management
  []
  (let [show-selected   (r/atom false)
        active-category (r/atom nil)]
    (fn []
      [:<>
       [header show-selected]
       (when @show-selected
         [selected-dishes])
       [dishes-by-categories active-category]
       [footer]])))

(defmethod app.routes/pages :admin-daily-crud [] menu-day-management)
