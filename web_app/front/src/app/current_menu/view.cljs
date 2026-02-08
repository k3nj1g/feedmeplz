(ns app.current-menu.view
  (:require ["lucide-react" :refer [Calendar Check Copy Minus Plus Search ShoppingCart]]
            [app.components.base :refer [button heading]]
            [app.components.card-parts :refer [card card-content card-header]]
            [app.components.text-input :refer [text-input]]

            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as r]

            [app.helpers    :as h]
            [app.utils.date :as date-utils]
            [app.routes     :as routes]

            [app.current-menu.controller :as controller]
            [app.current-menu.form       :as form]
            [app.current-menu.model      :as model]))

(defn quantity-controls [item]
  [:div.flex.items-center.gap-2.ml-4
   [:button.px-3.py-2.border.shadow.bg-white.rounded
    {:on-click #(dispatch [::controller/remove-from-cart item])}
    [:> Minus {:class "h-4 w-4"}]]
   [:span.w-8.text-center (get @(subscribe [::model/cart]) (:id item) 0)]
   [:button.px-3.py-2.border.shadow.bg-white.rounded
    {:on-click #(dispatch [::controller/add-to-cart item])}
    [:> Plus {:class "h-4 w-4"}]]])

#_(defn cart-content []
  (let [items-in-cart @(subscribe [::model/items-in-cart])
        cart-total @(subscribe [::model/cart-total])]
    [:div.mt-4
     (if (empty? items-in-cart)
       [:div.text-center.py-8.text-gray-500
        [:> ShoppingCart {:class "h-12 w-12 mx-auto mb-4 opacity-50"}]
        [:p "Корзина пуста"]]
       [:div.space-y-4
        (for [item items-in-cart]
          ^{:key (:id item)}
          [card
           [card-content {:class "p-4"}
            [:div.flex.justify-between.items-center
             [:div
              [:h3.font-medium (:name item)]
              [:p.text-sm.text-gray-500 (str (:price item) " ₽ × " (:quantity item))]]
             [quantity-controls item]]]])
        [:div.border-t.pt-4
         [:div.flex.justify-between.items-center.mb-4
          [:span.font-medium "Итого:"]
          [:span.font-medium (str cart-total " ₽")]]
         [button {:class "w-full"} "Оформить заказ"]]])]))

(defn header
  [{:keys [date]}]
  (let [cart-total @(subscribe [::model/cart-total])]
    [:div.flex.justify-between.mb-6
     [:div.flex.items-center.gap-4
      [heading
       "Меню на"]
      [:div.flex.items-center.text-base.border.rounded-lg.border-gray-200.px-2.gap-1.h-8
       [:> Calendar {:class "w-4 h-4"}]
       [:span.font-semibold (date-utils/->ru-verbose date)]]]
     [:div.flex.gap-4
      [text-input form/form-path [:search]
       {:adornment [:> Search {:class "w-5 h-5 text-gray-400 mr-2"}]
        :props     {:placeholder "Поиск блюд..."}}]
      [button
       {:type     "primary"
        :on-click #(js/console.log "Open cart")}
       [:> ShoppingCart {:class "h-4 w-4 mr-2"}]
       [:span (str cart-total " ₽")]]]]))

(defn menu-item
  [item]
  (let [cart @(subscribe [::model/cart])]
    [:div.flex.justify-between.items-center.p-3.rounded-lg.border
     (if (h/in? (:id item) (keys cart))
       {:class ["bg-blue-100" "border-blue-300"]}
       {:class ["bg-gray-50" "border-gray-50"]})
     [:div.flex.items-center.gap-4.flex-1
      [:div.flex-1
       [:span (:name item)]
       [:div.text-sm.text-gray-500
        (when-let [weight (:weight item)]
          [:span (str weight " г ")])
        (when (and (:weight item) (:kcals item))
          "·")
        (when-let [kcals (:kcals item)]
          [:span " " (str kcals " ккал")])]]]
     [:div.text-gray-600
      (str (:price item) " ₽")]
     [quantity-controls item]]))

(defn menu-category
  [category menu-items]
  [card {:class "mb-6"}
   [card-header
    category]
   [card-content
    [:div.grid.gap-2
     (for [item menu-items]
       ^{:key (:id item)}
       [menu-item item])]]])

(defn order-summary
  []
  (let [dragging-portion (r/atom nil)
        drag-over-zone   (r/atom nil)]
    (fn []
      (let [{:keys [dishes-total items-in-cart portions containers
                    containers-mode containers-count
                    used-containers-count unassigned-portions-count on-click]}
            @(subscribe [::model/order-summary])
            copied? @(subscribe [:db/get [:page :copied]])
            assigned-portions-count (- (count portions) unassigned-portions-count)
            free-portions (->> portions
                               (filter #(nil? (:container-number %)))
                               (sort-by (juxt :name :portion-index)))
            portions-by-container (->> portions
                                       (remove #(nil? (:container-number %)))
                                       (group-by :container-number))
            containers-map (into {} (map (juxt :container-number :items) containers))
            handle-drop (fn [event container-number]
                          (.preventDefault event)
                          (let [item-id (js/Number (.getData (.-dataTransfer event) "application/container-item"))
                                portion-index (js/Number (.getData (.-dataTransfer event) "application/container-portion-index"))]
                            (when (and (pos? item-id) (not (js/isNaN portion-index)))
                              (dispatch [::controller/set-item-container item-id portion-index container-number]))
                            (reset! drag-over-zone nil)
                            (reset! dragging-portion nil)))
            zone-class (fn [base-class zone-key]
                         (cond-> [base-class "transition-all"]
                           @dragging-portion (conj "border-dashed")
                           (= @drag-over-zone zone-key) (conj "ring-2 ring-blue-400 border-blue-400 bg-blue-50")))
            portion-chip (fn [portion]
                           (let [dragging? (= (:portion-key portion) @dragging-portion)]
                             ^{:key (:portion-key portion)}
                             [:div.inline-flex.items-center.gap-1.text-xs.border.rounded-full.bg-white.px-2.py-1.cursor-grab.group.transition-all
                              {:class (cond
                                        dragging? "border-blue-300 bg-blue-50 opacity-60 scale-95"
                                        :else "border-gray-200")
                               :draggable true
                               :on-drag-start #(do
                                                 (reset! dragging-portion (:portion-key portion))
                                                 (.setData (.-dataTransfer %) "application/container-item" (str (:item-id portion)))
                                                 (.setData (.-dataTransfer %) "application/container-portion-index" (str (:portion-index portion))))
                               :on-drag-end #(do
                                               (reset! drag-over-zone nil)
                                               (reset! dragging-portion nil))}
                              [:span.text-gray-400.opacity-0.group-hover:opacity-100.transition-opacity "⋮⋮"]
                              [:span (:name portion)]
                              [:span.text-gray-400.pr-1 (str "#" (inc (:portion-index portion)))]]))]
        (when (pos? dishes-total)
          [card
           [card-header
            "Сводка заказа"]
           [card-content
            [:div.space-y-4
             [:div.flex.items-center.justify-between
              [:div.text-sm.text-gray-500
               "Контейнеры необязательны. Распределяйте порции только при необходимости."]
              [button
               {:type     (if containers-mode "secondary" "primary")
                :class    "whitespace-nowrap"
                :on-click #(dispatch [::controller/toggle-container-mode])}
               (if containers-mode
                 "Скрыть распределение"
                 "Распределить по контейнерам")]]
             (if containers-mode
               [:<>
                [:div.flex.flex-wrap.items-center.justify-between.gap-2.bg-gray-50.border.rounded-lg.p-3.text-sm
                 [:div.flex.flex-wrap.items-center.gap-2
                  [:span.text-gray-600 "Контейнеров: " [:span.font-medium used-containers-count]]
                  [:span.text-gray-600 "Распределено порций: " [:span.font-medium assigned-portions-count]]
                  [:span.text-gray-600 "Свободных: " [:span.font-medium unassigned-portions-count]]]
                 [button
                  {:type     "secondary"
                   :class    "text-xs px-2 py-1"
                   :disabled (zero? assigned-portions-count)
                   :on-click #(dispatch [::controller/clear-container-assignments])}
                  "Сбросить распределение"]]
                [:div.text-xs.text-gray-500
                 "Подсказка: перетяните порцию по маркеру ⋮⋮ в нужную зону."]
                [:div.grid.gap-4.lg:grid-cols-2
                 [:div
                  {:class       (zone-class "border rounded-lg p-3 bg-amber-50 min-h-110px" :free)
                   :on-drag-over #(do
                                    (.preventDefault %)
                                    (reset! drag-over-zone :free))
                   :on-drop     #(handle-drop % nil)}
                  [:div.flex.items-center.justify-between.mb-2
                   [:span.text-sm.font-medium "Не распределено"]
                   [:span.text-xs.text-gray-500 (str (count free-portions) " шт")]]
                  (if (seq free-portions)
                    [:div.flex.flex-wrap.gap-2
                     (for [portion free-portions]
                       (portion-chip portion))]
                    [:div.text-xs.text-gray-500 "Перетащите сюда порции, которые не нужно класть в контейнер"])]
                 [:div.space-y-3
                  (doall
                   (for [container-number (range 1 (inc containers-count))]
                     (let [container-portions (sort-by (juxt :name :portion-index)
                                                       (get portions-by-container container-number []))
                           container-summary  (get containers-map container-number)]
                       ^{:key container-number}
                       [:div
                        {:class       (zone-class "border rounded-lg p-3 bg-white min-h-110px" [:container container-number])
                         :on-drag-over #(do
                                          (.preventDefault %)
                                          (reset! drag-over-zone [:container container-number]))
                         :on-drop     #(handle-drop % container-number)}
                        [:div.flex.items-center.justify-between.mb-2
                         [:span.font-medium (str "Контейнер " container-number)]
                         [:div.flex.items-center.gap-2
                          [:span.text-xs.text-gray-500 (str (count container-portions) " шт")]
                          (when (> containers-count 1)
                            [button
                             {:type     "secondary"
                              :class    "text-xs px-2 py-1"
                              :on-click #(dispatch [::controller/remove-container container-number])}
                             "Удалить"])]]
                        (if (seq container-portions)
                          [:<>
                           [:div.flex.flex-wrap.gap-2.mb-2
                            (for [portion container-portions]
                              (portion-chip portion))]
                           [:div.space-y-1
                            (for [item container-summary]
                              ^{:key (:item-id item)}
                              [:div.text-xs.text-gray-700.flex.justify-between
                               [:span (:name item) " × " (:quantity item)]
                               [:span (str (controller/format-rub (:line-total item)) " ₽")]])]]
                          [:div.text-xs.text-gray-400 "Перетащите порции сюда"])])))
                  [:div.flex.items-center.justify-between
                   [:div]
                   [button
                    {:type     "secondary"
                     :class    "text-xs px-2 py-1"
                     :on-click #(dispatch [::controller/add-container])}
                    "+ Контейнер"]]]]]
               [:div.space-y-2
                (for [item items-in-cart]
                  ^{:key (:id item)}
                  [:div.flex.justify-between.text-sm
                   [:span (:name item) " × " (:quantity item)]
                   [:span (str (controller/format-rub (* (:quantity item) (:price item))) " ₽")]])])
             [:div.border-t.pt-2.mt-2.space-y-1
              [:div.flex.justify-between.font-medium
               [:span "Итого:"]
               [:span (str (controller/format-rub dishes-total) " ₽")]]]
             [button
              {:type     (if copied? "success" "primary")
               :class    "w-full"
               :on-click on-click}
              (if copied?
                [:<>
                 [:> Check {:class "h-4 w-4 mr-2"}]
                 "Скопировано!"]
                [:<>
                 [:> Copy {:class "h-4 w-4 mr-2"}]
                 "Копировать заказ"])]]]])))))

(defn empty-state
  []
  [:div.flex.flex-col.items-center.justify-center.p-8.bg-gray-50.rounded-lg.border-2.border-dashed.border-gray-200
   [:> ShoppingCart {:class "w-12 h-12 text-gray-400 mb-4"}]
   [:h3.text-lg.font-medium.text-gray-900.mb-1 "Меню не найдено"]])

(defn current-menu-view
  []
  (let [{:keys [menu items]} @(subscribe [::model/daily-menu])] 
    (if menu
      [:<>
       [header menu]
       (for [[category-name menu-items] items]
         ^{:key category-name}
         [menu-category category-name menu-items])
       [order-summary]]
      [empty-state])))

(defmethod routes/pages :current-menu [] current-menu-view)
