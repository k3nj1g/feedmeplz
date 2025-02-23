(ns app.layout.navbar
  (:require ["lucide-react" :refer [Clock User Settings ChevronDown]]
            
            [re-frame.core :refer [dispatch subscribe]]

            [app.components.base :refer [button]]
            
            [app.components.dropdown :refer [dropdown]]
            
            [app.auth.events :as auth]))

(defn admin-menu-item
  [label page-name]
  [:button.block.px-4.py-2.text-sm.text-gray-700.hover:bg-gray-100.w-full.text-left
   {:on-click #(dispatch [:navigate page-name])}
   label])

(defn admin-menu
  []
  [dropdown
   {:popup-id  :admin
    :trigger   {:tag     :button
                :props   {:class ["inline-flex" "items-center" "text-gray-500"]}
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
    :on-click #(dispatch [:navigate page-name])}
   [:> icon {:class ["w-5" "h-5" "mr-2"]}]
   label])

(defn navigation
  [current-page]
  (let [{:keys [authenticated? user-info]} @(subscribe [:db/get [:auth]])]
    [:nav.bg-white.shadow-sm
     [:div.max-w-6xl.mx-auto.px-4
      [:div.flex.justify-between.h-16
       [:div.flex.space-x-8
        [nav-button current-page :current-menu Clock "Меню дня"]
        #_[nav-button current-page :orders ShoppingCart "Мои заказы"]
        (when authenticated?
          [admin-menu])]
       [:div.flex.items-center
        (if authenticated?
          [dropdown
           {:popup-id  :user
            :trigger   {:tag     :button
                        :props   {:class ["inline-flex" "items-center"]}
                        :content [:<>
                                  [:> User {:class ["w-5" "h-5" "text-gray-500"]}]
                                  [:span.ml-2.text-gray-700 (:username user-info)]]}
            :content   [:div.py-1
                        [:button.block.px-4.py-2.text-sm.text-gray-700.hover:bg-gray-100.w-full.text-left
                         {:on-click #(dispatch [::auth/logout])}
                         "Выйти"]]
            :placement :top-right}]
          [button
           {:type     "primary"
            :on-click #(dispatch [:navigate :login])}
           "Войти"])]]]]))
