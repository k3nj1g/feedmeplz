(ns app.home.view
  (:require [app.routes :as routes]
            [re-frame.subs :refer [subscribe]]
            
            [app.home.model]))

(defn home-panel []
  (let [data @(subscribe [:home])]
    [:<>
     [:h1 "Меню"]
     (into
      [:<>]
      (map
       (fn [[category dishes]]
         [:<> 
          [:h2 category]
          [:table
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
             dishes))]])

       data))]))

(defmethod routes/pages :home [] [home-panel])
