(ns app.telegram.order
  (:require [re-frame.core :as rf]
            [app.telegram.core :as telegram]
            [app.telegram.auth :as telegram-auth]
            [app.telegram.styles :as styles]
            [app.components.base :refer [button]]
            [app.components.card-parts :refer [card card-content]]))

(defn order-item [{:keys [name price quantity]}]
  [:div.flex.justify-between.items-center.py-2
   [:div.flex-1
    [:span.text-gray-900 name]
    [:span.text-gray-500.text-sm.ml-2 (str "×" quantity)]]
   [:span.text-gray-900 (str price " ₽")]])

(defn order-summary []
  (let [items-in-cart @(rf/subscribe [:items-in-cart])
        cart-total @(rf/subscribe [:cart-total])]
    [:div.bg-white.rounded-lg.shadow-sm.p-4.mb-4
     [:h3.font-medium.mb-3 "Ваш заказ"]
     [:div.space-y-2
      (for [item items-in-cart]
        ^{:key (:id item)}
        [order-item item])]
     [:div.border-t.border-gray-200.mt-3.pt-3
      [:div.flex.justify-between.items-center
       [:span.font-medium "Итого:"]
       [:span.font-medium (str cart-total " ₽")]]]]))

(defn order []
  [:div.p-4.pb-20
   [:div.flex.items-center.mb-4
    [:button.p-2.mr-2
     {:on-click #(rf/dispatch [:navigate :telegram-cart])}
     [:> ArrowLeft {:class "w-5 h-5"}]]
    [:h1.text-xl.font-bold "Отправить заказ"]]
   
   [order-summary]
   
   [:div.fixed.bottom-0.left-0.right-0.bg-white.border-t.border-gray-200.p-4
    [button
     {:type "primary"
      :class "w-full"
      :on-click #(rf/dispatch [:send-telegram-order])}
     "Отправить в чат"]]]) 