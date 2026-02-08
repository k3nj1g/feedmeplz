(ns app.admin.daily.import.model
  (:require [re-frame.core :refer [dispatch reg-sub]]

            [app.admin.daily.import.controller]
            [app.admin.daily.import.form :as form]))

;; Бизнес-логика хранится под :admin-daily-import в app-db
(def business-logic-path [:admin-daily-import])

(reg-sub
 :admin-daily-import/status
 (fn [db _]
   (get-in db (conj business-logic-path :status))))

(reg-sub
 ::import-file
 :<- [:zf/get-value form/form-path [:file]]
 (fn [value _]
   value))

(reg-sub
 :admin-daily-import/validation-result
 (fn [db _]
   (get-in db (conj business-logic-path :validation-result))))

(reg-sub
 :admin-daily-import/create-new-dishes?
 (fn [db _]
   (get-in db (conj business-logic-path :create-new-dishes?))))

(reg-sub
 :admin-daily-import/error
 (fn [db _]
   (get-in db (conj business-logic-path :error))))

(reg-sub
 :admin-daily-import/import-result
 (fn [db _]
   (get-in db (conj business-logic-path :import-result))))

(reg-sub
 :admin-daily-import/can-import?
 :<- [:admin-daily-import/validation-result]
 :<- [:admin-daily-import/status]
 (fn [[validation-result status] _]
   (and validation-result
        (= (:status validation-result) :valid)
        (not (:existing-menu? validation-result))
        (= status :validated))))

(reg-sub
 ::validate-button
 :<- [::import-file]
 :<- [:admin-daily-import/status]
 :<- [:admin-daily-import/validation-result]
 (fn [[file status validation-result] _]
   {:file file
    :status status
    :validation-result validation-result}))
