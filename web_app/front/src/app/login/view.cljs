(ns app.login.view 
  (:require [app.components.base       :refer [button]]
            [app.components.card-parts :refer [card card-content card-description
                                               card-header card-title]]
            [app.components.text-input :refer [text-input]]

            [app.routes :as routes]

            [app.login.form  :as form]
            [app.login.model :as model]))

(defn handle-submit [event]
  (.preventDefault event)
  (model/on-submit))

(defn handle-key-down [event]
  (when (= (.-key event) "Enter")
    (.preventDefault event)
    (model/on-submit)))

(defn login-view
  []
  [:div.min-h-screen.bg-gray-50.flex.items-center.justify-center.py-12.px-4.sm:px-6.lg:px-8
   [card
    {:class ["w-full" "max-w-md"]}
    [card-header
     {:class ["space-y-1" "text-center"]}
     [card-title
      "Вход в систему"]
     [card-description
      "Войдите в систему заказа обедов"]]
    [card-content
     [:form.grid.gap-4 {:on-key-down handle-key-down}
      [text-input form/form-path [:username]]
      [text-input form/form-path [:password] {:props {:type "password"}}]
      [button
       {:type "primary" :on-click handle-submit}
       "Войти"]]]]])

(defmethod routes/pages :login [] login-view)
