(ns app.telegram.menu
  (:require [re-frame.core :as rf]
            [app.telegram.core :as telegram]
            [app.telegram.auth :as telegram-auth]
            [app.telegram.styles :as styles]
            [app.components.base :refer [button]]))

(defn menu-item [{:keys [id name description price image]}]
  [:div.bg-white.rounded-lg.shadow-sm.p-4.mb-4
   [:div.relative.aspect-w-16.aspect-h-9.mb-3
    [:img.w-full.h-48.object-cover.rounded-lg {:src image :alt name}]]
   [:div.flex.justify-between.items-start.mb-2
    [:div.flex-1
     [:h3.text-lg.font-medium.mb-1 name]
     [:p.text-sm.text-gray-500 description]]
    [:span.text-lg.font-bold.text-blue-500 (str price " ₽")]]
   [:button.w-full.bg-blue-500.text-white.py-2.px-4.rounded-lg.font-medium
    {:on-click #(rf/dispatch [:add-to-cart id])}
    "В корзину"]])

(defn user-info []
  (let [user @(rf/subscribe [:telegram-user])]
    (when user
      [:div.bg-white.rounded-lg.shadow-sm.p-4.mb-4
       [:div.flex.items-center
        [:div.flex-1
         [:h3.font-medium (str (:first-name user) " " (:last-name user))]
         [:p.text-sm.text-gray-500 (str "@" (:username user))]]]])))

(defn search-bar []
  [:div.mb-4
   [:div.relative
    [:input.w-full.px-4.py-2.pl-10.border.border-gray-300.rounded-lg
     {:type "text"
      :placeholder "Поиск блюд..."
      :on-change #(rf/dispatch [:set-search (.. % -target -value)])}]
    #_[:div.absolute.left-3.top-2.5
     [:> Search {:class "w-5 h-5 text-gray-400"}]]]])

(defn category-tabs []
  (let [categories @(rf/subscribe [:categories])
        active-category @(rf/subscribe [:active-category])]
    [:div.overflow-x-auto.mb-4
     [:div.flex.space-x-2.pb-2
      (for [category categories]
        ^{:key (:id category)}
        [:button.px-4.py-2.rounded-full.text-sm.font-medium
         {:class (if (= active-category (:id category))
                  "bg-blue-500 text-white"
                  "bg-gray-100 text-gray-700")
          :on-click #(rf/dispatch [:set-category (:id category)])}
         (:name category)])]]))

(defn menu []
  (let [menu-items @(rf/subscribe [:menu/items])]
    [:div.p-4.pb-20
     [user-info]
     [search-bar]
     [category-tabs]
     [:div.grid.grid-cols-1.gap-4
      (for [item menu-items]
        ^{:key (:id item)}
        [menu-item item])]
     [:div.fixed.bottom-0.left-0.right-0.bg-white.border-t.border-gray-200.p-4
      [:button.w-full.bg-green-500.text-white.py-3.px-4.rounded-lg.font-medium
       {:on-click #(rf/dispatch [:navigate :telegram-cart])}
       "Перейти в корзину"]]])) 
