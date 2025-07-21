(ns app.current-menu.model
  (:require [re-frame.core :refer [reg-sub]]

            [app.helpers :as h]

            [app.current-menu.controller :as ctrl]
            [app.current-menu.form       :as form]))

(reg-sub
 ::daily-menu
 :<- [:http/response ::ctrl/daily-menus]
 :<- [:http/response ::ctrl/categories]
 :<- [:zf/get-value form/form-path [:search]]
 (fn [[daily-menus categories search] _]
   (let [data (-> daily-menus :data (first))]
     {:menu  (:menu data)
      :items (->> data :menu-items
                  (group-by :category_id)
                  (reduce-kv
                   (fn [acc k v]
                     (conj acc
                           [(some #(when (= (:id %) k) (:name %)) categories)
                            (cond->> v
                              search
                              (filter #(h/match-search-term? (:name %) search)))])) []))})))

(reg-sub
 ::cart
 (fn [db _]
   (get-in db [:page :cart])))

(reg-sub
 ::menu-items
 :<- [::daily-menu]
 (fn [{:keys [items]} _]
   (->> items
        (flatten)
        (filter map?))))

(reg-sub
 ::cart-total
 :<- [::menu-items]
 :<- [::cart]
 (fn [[menu-items cart] _]
   (reduce-kv (fn [total id quantity]
                (let [item (first (filter #(= (:id %) id) menu-items))]
                  (+ total (* (:price item) quantity))))
              0
              cart)))

(reg-sub
 ::items-in-cart
 :<- [::menu-items]
 :<- [::cart]
 (fn [[menu-items cart]]
   (map (fn [[id quantity]]
          (let [item (first (filter #(= (:id %) id) menu-items))]
            (assoc item :quantity quantity)))
        cart)))

(reg-sub
 ::order-summary
 :<- [::cart-total]
 :<- [::items-in-cart]
 (fn [[cart-total items-in-cart] _]
   {:cart-total    cart-total
    :items-in-cart items-in-cart
    :on-click      (h/action [::ctrl/copy-order-to-clipboard cart-total items-in-cart])}))
