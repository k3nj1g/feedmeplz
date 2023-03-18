(ns app.view
  (:require ["@heroicons/react/24/outline" :refer [UserCircleIcon]]))

(defn navbar []
  [:nav.bg-gray-800
   [:div.mx-auto.max-w-7xl.px-2.sm:px-6.lg:px-8
    [:div.relative.flex.h-16.items-center.justify-between
     [:div.absolute.inset-y-0.left-0.flex.items-center.sm:hidden
      [:button.:inline-flex.items-center.justify-center.rounded-md.p-2.text-gray-400.hover:bg-gray-700.hover:text-white.focus:outline-none.focus:ring-2.focus:ring-inset.focus:ring-white
       {:type          "button"
        :aria-controls "mobile-menu"
        :aria-expanded "false"}
       [:span.sr-only "Open main menu"]
       [:svg.block.h-6.w-6
        {:xmlns        "http://www.w3.org/2000/svg"
         :fill         "none"
         :view-box     "0 0 24 24"
         :stroke-width "1.5"
         :stroke       "currentColor"
         :aria-hidden  "true"}
        [:path
         {:stroke-linecap  "round"
          :stroke-linejoin "round"
          :d               "M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5"}]]
       [:svg.hidden.h-6.w-6
        {:xmlns        "http://www.w3.org/2000/svg"
         :fill         "none"
         :view-box     "0 0 24 24"
         :stroke-width "1.5"
         :stroke       "currentColor"
         :aria-hidden  "true"}
        [:path
         {:stroke-linecap  "round"
          :stroke-linejoin "round"
          :d               "M6 18L18 6M6 6l12 12"}]]]]
     [:div.flex.flex-1.items-center.justify-center.sm:items-stretch.sm:justify-start
      [:div.flex.flex-shrink-0.items-center
       [:img.h-10
        {:src "/images/feedmeplz.png" }]]
      [:div.hidden.sm:ml-6.sm:block
       [:div.flex.space-x-4
        [:a.bg-gray-900.text-white.px-3.py-2.rounded-md.text-sm.font-medium
         {:href         "#"
          :aria-current "page"}
         "Заказ еды"]]]]
     [:div.absolute.inset-y-0.right-0.flex.items-center.pr-2.sm:static.sm:inset-auto.sm:ml-6.sm:pr-0
      [:div.relative.ml-3
       [:div
        [:button#user-menu-button.flex.rounded-full.bg-gray-800.text-sm.focus:outline-none.focus:ring-2.focus:ring-white.focus:ring-offset-2.focus:ring-offset-gray-800
         {:type          "button"
          :aria-expanded "false"
          :aria-haspopup "true"}
         [:span.sr-only "Open user menu"]
         [:> UserCircleIcon {:class [:h-8 :w-8 :text-white]}]]]]]]]
   [:div#mobile-menu.sm:hidden
    [:div.space-y-1.px-2.pt-2.pb-3
     [:a.bg-gray-900.text-white.block.px-3.py-2.rounded-md.text-base.font-medium
      {:href         "#"
       :aria-current "page"} "Заказ еды"]]]])
