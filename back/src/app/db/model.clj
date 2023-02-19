(ns app.db.model
  (:require [buddy.hashers :as hash]

            [gungnir.model :as gm]))

(def account-model
  [:map {}
   [:account/id {:primary-key true} int?]
   [:account/username string?]
   [:account/nickname string?]
   [:account/password {:before-save [:bcrypt]} [:string {:min 6}]]
   [:account/password-confirmation {:virtual true} [:string {:min 6}]]
   [:account/last-login inst?]
   [:account/created-at {:auto true} inst?]
   [:account/updated-at {:auto true} inst?]])

(def category-model
  [:map {}
   [:category/id {:primary-key true} int?]
   [:category/title string?]
   [:category/created-at {:auto true} inst?]
   [:category/updated-at {:auto true} inst?]])

(def menu-model
  [:map {:has-one {:menu/fk-category {:model :category :foreign-key :category/id}}}
   [:menu/id {:primary-key true} int?]
   [:menu/fk-category int?]
   [:menu/title string?]
   [:menu/price float?]
   [:menu/kcal float?]
   [:menu/weight float?]
   [:menu/created-at {:auto true} inst?]
   [:menu/updated-at {:auto true} inst?]])

(defmethod gm/before-save :bcrypt [_k v]
  (buddy.hashers/derive v))

(gungnir.model/register!
 {:account  account-model
  :category category-model
  :menu     menu-model})
