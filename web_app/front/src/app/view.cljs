(ns app.view
  (:require ["@heroicons/react/24/outline" :refer [UserCircleIcon]]
            ["bootstrap"]
            
            [re-frame.core :refer [subscribe]]
            
            [app.subs   :as subs]
            [app.routes :as routes]
            
            [app.home.view]))

(defn navbar []
  [:nav.navbar.navbar-expand-lg.navbar-dark.bg-dark
   [:div.container-fluid
    [:button.navbar-toggler
     {:type           "button"
      :data-bs-toggle "collapse"
      :data-bs-target "#navbarToggler"
      :aria-controls  "navbarToggler"
      :aria-expanded  "false"
      :aria-label     "Toggle navigation"}
     [:span.navbar-toggler-icon]]
    [:div#navbarToggler.collapse.navbar-collapse
     [:a.navbar-brand.d-flex.align-items-center
      {:href "#"}
      [:img.d-inline-block.align-top.h--24.w--24
       {:src "assets/images/feedmeplz.png"}]
      [:span.ms-1 "Заказ еды"]]
     [:ul.navbar-nav.me-auto.mb-2.mb-lg-0
      [:li.nav-item
       [:a.nav-link {:href "#/home"} "Home"]]
      [:li.nav-item
       [:a.nav-link {:href "#/about"} "About"]]]]]])

(defn layout-view []
  (let [active-page @(subscribe [::subs/active-page])]
    [:<>
     [navbar]
     (routes/pages active-page)]))
