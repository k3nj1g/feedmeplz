(ns app.current-menu.view
  (:require ["lucide-react" :refer [Calendar Check Copy Minus Plus Search ShoppingCart]]
            [app.components.base :refer [button heading]]
            [app.components.card-parts :refer [card card-content card-header]]
            [app.components.text-input :refer [text-input]]

            [re-frame.core :refer [dispatch subscribe]]

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
  (let [{:keys [cart-total items-in-cart on-click]} @(subscribe [::model/order-summary])
        copied? @(subscribe [:db/get [:page :copied]])]
    (when (pos? cart-total)
      [card
       [card-header
        "Сводка заказа"]
       [card-content
        [:div.space-y-2
         (for [item items-in-cart]
           ^{:key (:id item)}
           [:div.flex.justify-between.text-sm
            [:span (:name item) " × " (:quantity item)]
            (str (* (:price item) (:quantity item)) " ₽")])
         [:div.border-t.pt-2.mt-2.flex.justify-between.font-medium
          [:span "Итого:"]
          [:span (str cart-total " ₽")]]
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
             "Копировать заказ"])]]]])))

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
