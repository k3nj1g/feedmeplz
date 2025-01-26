(ns app.admin.daily.view
  (:require ["lucide-react" :refer [Plus X Save ChevronDown Search List]]
            [re-frame.core :refer [dispatch subscribe]]

            [reagent.core :as r]

            [app.components.base       :refer [button heading]]
            [app.components.card-parts :refer [card card-header card-content]]
            [app.components.text-input :refer [text-input]]

            [app.routes :as routes]

            [app.helpers :as h]

            [app.admin.daily.form  :as form]
            [app.admin.daily.model :as model]))

(def items-per-page 10)

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
            selected-items @(subscribe [:zf/get-value form/form-path [(form/category->path category) :dishes]])
            set-value      #(dispatch [:zf/set-value form/form-path [(form/category->path category) :dishes] %])]
        [:div
         [:button.w-full.bg-white.border.rounded-lg.shadow-sm
          {:on-click #(reset! active-category (if (= @active-category category) nil category))}
          [:div.flex.justify-between.items-center.p-4
           [:h3.font-semibold.leading-none.tracking-tight
            {:on-click #(reset! active-category (if (= % category) nil category))}
            (:name category)]
           [:div.flex.items-center
            [:span.mr-2.text-sm.text-gray-500 "Выбрано: 0"]
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
                  {:class    (if (h/in? (:id item) selected-items)
                               "bg-blue-100 border-blue-300"
                               "bg-white border-gray-200")
                   :on-click #(if (h/in? (:id item) selected-items)
                                (set-value (vec (remove (partial = (:id item)) selected-items)))
                                (set-value (vec (conj (or selected-items []) (:id item)))))}
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
