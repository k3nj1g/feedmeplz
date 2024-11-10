(ns app.menu.view
  (:require [app.routes :as routes]
            [re-frame.subs :refer [subscribe]]
            
            [app.menu.model]))

(defn menu-category-panel
  [title content]
  (let [id (hash title)]
    [:div.accordion-item
     [:h2.accordion-header
      [:button.accordion-button
       {:type           "button"
        :data-bs-toggle "collapse"
        :data-bs-target (str "#" id)
        :aria-expanded  "true"
        :aria-controls  id}
       title]]
     [:div.accordion-collapse.collapse.show
      {:id             id
       :data-bs-parent "#accordionMenu"}
      [:div.accordion-body
       content]]]))

(defn menu-panel
  []
  (let [data @(subscribe [:home])]
    [:div.container
     [:h1 "Меню"]
     (into
      [:div#accordionMenu.accordion]
      (map
       (fn [[category dishes]]
         [:<>
          [menu-category-panel category
           [:table.table
            [:thead
             [:tr
              [:th "Наименование"]
              [:th "Вес"]
              [:th "Калорийность"]
              [:th "Стоимость"]]]
            (into
             [:tbody]
             (map
              (fn [{:keys [title weight kcal price]}]
                [:tr
                 [:td title]
                 [:td weight]
                 [:td kcal]
                 [:td price]])
              dishes))]]])

       data))]))

(defmethod routes/pages :menu [] [menu-panel])
