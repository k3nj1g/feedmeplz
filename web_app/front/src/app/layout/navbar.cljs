(ns app.layout.navbar
  (:require ["lucide-react" :refer [Clock User Settings ShoppingCart ChevronDown]]
            
            [re-frame.core :as rf]
            
            [app.components.dropdown :refer [dropdown]]))

(defn admin-menu-item
  [label page-name]
  [:button.block.px-4.py-2.text-sm.text-gray-700.hover:bg-gray-100.w-full.text-left
   {:on-click #(do
                 #_(rf/dispatch [:set-active-section section-name])
                 (rf/dispatch [:navigate page-name]))}
   label])

(defn admin-menu
  []
  [dropdown
   {:popup-id  :admin
    :trigger   {:tag     :button
                :props   {:class (cond-> ["inline-flex" "items-center"]
                                   true
                                   (conj "border-transparent" "text-gray-500"))}
                :content [:<>
                          [:> Settings {:class ["w-5" "h-5" "mr-2"]}]
                          "Управление"
                          [:> ChevronDown {:class ["w-4" "h-4" "ml-1"]}]]}
    :content   [:div.py-1
                [admin-menu-item "Каталог блюд" :admin-catalog]
                [admin-menu-item "Меню дня" :admin-daily-list]
                #_[admin-menu-item "Пользователи" :admin-users]
                #_[admin-menu-item "Роли" :admin-roles]]
    :placement :top}])

(defn nav-button
  [current-page page-name icon label]
  [:button
   {:class    (into ["inline-flex" "items-center" "px-1" "border-b-2"]
                    (if (= current-page page-name)
                      ["border-blue-500" "text-gray-900"]
                      ["border-transparent" "text-gray-500"]))
    :on-click #(rf/dispatch [:navigate page-name])}
   [:> icon {:class ["w-5" "h-5" "mr-2"]}]
   label])

(defn navigation
  [current-page]
  [:nav.bg-white.shadow-sm
   [:div.max-w-6xl.mx-auto.px-4
    [:div.flex.justify-between.h-16
     [:div.flex.space-x-8
      [nav-button current-page :current-menu Clock "Меню дня"]
      #_[nav-button current-page :orders ShoppingCart "Мои заказы"]
      [admin-menu]]
     [:div.flex.items-center
      [:> User {:class ["w-5" "h-5" "text-gray-500"]}]
      [:span.ml-2.text-gray-700 "Иван Петров"]]]]])
