(ns app.layout.navbar
  (:require ["lucide-react" :refer [Calendar Clock User Users Settings ShoppingCart ChevronDown]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [app.routes :as routes]))

(defn admin-menu-item
  [label section-name]
  [:button.block.px-4.py-2.text-sm.text-gray-700.hover:bg-gray-100.w-full.text-left
   {:on-click #(do
                 (rf/dispatch [:set-active-section section-name])
                 (rf/dispatch [:toggle-admin-menu false]))}
   label])

(defn admin-menu []
  (let [show-menu (r/atom false)]
    (fn []
      [:div.relative.inline-flex
        [:button.inline-flex.items-center
         {:class (if false #_(clojure.string/starts-with? (name @active-section) "admin")
                     [:border-blue-500 :text-gray-900]
                     [:border-transparent :text-gray-500])
          :on-click #(swap! show-menu not)}
         [:> Settings {:class [:w-5 :h-5 :mr-2]}]
         "Управление"
         [:> ChevronDown {:class [:w-4 :h-4 :ml-1]}]]
      
        (when @show-menu
          [:div.absolute.top-full.z-10.left-0.mt-2.w-56.rounded-md.shadow-lg.bg-white.ring-1.ring-black.ring-opacity-5
           [:div.py-1
            [admin-menu-item "Каталог блюд" :admin-catalog]
            [admin-menu-item "Меню дня" :admin-daily]
            [admin-menu-item "Пользователи" :admin-users]
            [admin-menu-item "Роли" :admin-roles]]])])))

(defn nav-button [current-page page-name icon label]
  [:button
   {:class    (into [:inline-flex :items-center :px-1 :pt-1 :border-b-2]
                    (if (= current-page page-name)
                      [:border-blue-500 :text-gray-900]
                      [:border-transparent :text-gray-500]))
    :on-click #(rf/dispatch [:navigate page-name])}
   [:> icon {:class [:w-5 :h-5 :mr-2]}]
   label])

(defn navigation
  [current-page]
  [:nav.bg-white.shadow-sm
   [:div.max-w-6xl.mx-auto.px-4
    [:div.flex.justify-between.h-16
     [:div.flex.space-x-8
      [nav-button current-page :menu Clock "Меню дня"]
      [nav-button current-page :orders ShoppingCart "Мои заказы"]
      [admin-menu]]
     [:div.flex.items-center
      [:> User {:class [:w-5 :h-5 :text-gray-500]}]
      [:span.ml-2.text-gray-700 "Иван Петров"]]]]])
