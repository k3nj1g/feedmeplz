(ns app.toast
  (:require
   ["react-toastify" :refer [toast]]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn show-toast
  [message type]
  (let [toast-options #js {}
        message       (if (vector? message)
                        (r/as-element message)
                        message)]
    (case type
      :success (toast.success message toast-options)
      :error   (toast.error message toast-options)
      :warning (toast.warning message toast-options)
      (toast.info message toast-options))))

(rf/reg-fx
 :toast
 (fn [{:keys [message type]}]
   (show-toast message type)))
