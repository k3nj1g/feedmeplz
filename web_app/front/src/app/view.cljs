(ns app.view
  (:require ["bootstrap"]
            
            [re-frame.core :refer [subscribe]]
            
            [app.subs   :as subs]
            [app.routes :as routes]
            
            [app.layout.navbar :refer [navigation]]
            
            [app.home.view]
            [app.menu.view]
            [app.admin.view]))

(defn layout-view []
  (let [active-page @(subscribe [::subs/active-page])]
    [:<>
     [navigation active-page]
     [:div.max-w-6xl.mx-auto.px-4.py-6
      (routes/pages active-page)]]))
