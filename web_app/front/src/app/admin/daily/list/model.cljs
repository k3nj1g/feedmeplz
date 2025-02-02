(ns app.admin.daily.list.model
  (:require [re-frame.core :refer [reg-sub]]

            [app.admin.daily.list.controller :as ctrl]))

(reg-sub
 ::daily-menus
 :<- [:http/response ::ctrl/daily-menus]
 (fn [daily-menus _]
   daily-menus))

(reg-sub
 ::categories
 :<- [:http/response ::ctrl/categories]
 (fn [categories _]
   categories))
