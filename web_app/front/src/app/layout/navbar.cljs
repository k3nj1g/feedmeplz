(ns app.layout.navbar
  (:require [app.routes :as routes]))

(defn nav-item
  [path title]
  [:li.nav-item
   [:a.nav-link
    {:href (routes/url-for path)}
    title]])

(defn navbar
  []
  [:nav.navbar.navbar-expand-lg.navbar-dark.bg-secondary
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
      {:href (routes/url-for :home)}
      [:img.d-inline-block.align-top.h--24.w--24
       {:src "assets/images/feedmeplz.png"}]
      [:span.ms-1 "Заказ еды"]]
     [:ul.navbar-nav.me-auto.mb-2.mb-lg-0
      [nav-item :menu "Меню"]]]]])
