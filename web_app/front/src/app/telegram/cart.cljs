(ns app.telegram.cart
  (:require ["lucide-react" :refer [ArrowLeft Calendar Check Copy Minus Plus Search ShoppingCart]]
            
            [re-frame.core :as rf]
            [app.telegram.core :as telegram]
            [app.telegram.auth :as telegram-auth]
            [app.telegram.styles :as styles]
            [app.components.base :refer [button]]
            [app.components.card-parts :refer [card card-content]]))

(defn cart-item [{:keys [id name price quantity]}]
  [:div.bg-white.rounded-lg.shadow-sm.p-4.mb-2
   [:div.flex.justify-between.items-center
    [:div.flex-1
     [:h3.font-medium name]
     [:div.flex.items-center.mt-1
      [:span.text-sm.text-gray-500 (str price " ₽")]
      [:span.text-sm.text-gray-400.mx-2 "×"]
      [:span.text-sm.text-gray-500 quantity]]]
    [:div.flex.items-center.gap-2
     [:button.p-2.text-gray-500.hover:text-red-500
      {:on-click #(rf/dispatch [:remove-from-cart id])}
      [:> Minus {:class "w-4 h-4"}]]]]])

(defn user-info []
  (let [user @(rf/subscribe [:telegram-user])]
    (when user
      [:div.bg-white.rounded-lg.shadow-sm.p-4.mb-4
       [:div.flex.items-center
        [:div.flex-1
         [:h3.font-medium (str (:first-name user) " " (:last-name user))]
         [:p.text-sm.text-gray-500 (str "@" (:username user))]]]])))

(defn empty-cart []
  [:div.flex.flex-col.items-center.justify-center.py-12
   [:div.w-16.h-16.bg-gray-100.rounded-full.flex.items-center.justify-center.mb-4
    [:> ShoppingCart {:class "w-8 h-8 text-gray-400"}]]
   [:h3.text-lg.font-medium.text-gray-900.mb-1 "Корзина пуста"]
   [:p.text-sm.text-gray-500.mb-6 "Добавьте блюда из меню"]
   [:button.bg-blue-500.text-white.py-2.px-4.rounded-lg.font-medium
    {:on-click #(rf/dispatch [:navigate :telegram-menu])}
    "Перейти в меню"]])

(defn cart []
  (let [items-in-cart @(rf/subscribe [:items-in-cart])
        cart-total @(rf/subscribe [:cart-total])]
    [:div.p-4.pb-20
     [user-info]
     [:div.flex.items-center.mb-4
      [:button.p-2.mr-2
       {:on-click #(rf/dispatch [:navigate :telegram-menu])}
       [:> ArrowLeft {:class "w-5 h-5"}]]
      [:h1.text-xl.font-bold "Корзина"]]

     (if (empty? items-in-cart)
       [empty-cart]
       [:<>
        [:div.space-y-2
         (for [item items-in-cart]
           ^{:key (:id item)}
           [cart-item item])]

        [:div.fixed.bottom-0.left-0.right-0.bg-white.border-t.border-gray-200.p-4
         [:div.flex.justify-between.items-center.mb-4
          [:span.font-medium "Итого:"]
          [:span.font-medium (str cart-total " ₽")]]
         [button
          {:type "primary"
           :class "w-full"
           :on-click #(rf/dispatch [::send-telegram-order items-in-cart])}
          "Оформить заказ"]]])]))
