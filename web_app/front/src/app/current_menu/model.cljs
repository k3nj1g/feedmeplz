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
 (fn [[[daily-menus] categories search] _]
   {:menu  (:menu daily-menus)
    :items (->> daily-menus :menu-items
                (group-by :category_id)
                (reduce-kv
                 (fn [acc k v]
                   (conj acc
                         [(some #(when (= (:id %) k) (:name %)) categories)
                          (cond->> v
                            search
                            (filter #(h/match-search-term? (:name %) search)))])) []))}))

(reg-sub
 ::cart
 (fn [db _]
   (get-in db [:page :cart])))

(reg-sub
 ::cart-total
 :<- [::daily-menu]
 :<- [::cart]
 (fn [[daily-menu cart] _]
   (reduce-kv (fn [total id quantity]
                (let [item (first (filter #(= (:id %) id) (flatten (vals daily-menu))))]
                  (+ total (* (:price item) quantity))))
              0
              cart)))

(reg-sub
 ::items-in-cart
 :<- [::daily-menu]
 :<- [::cart]
 (fn [[daily-menu cart]]
   (map (fn [[id quantity]]
          (let [item (first (filter #(= (:id %) id) (flatten (vals daily-menu))))]
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
