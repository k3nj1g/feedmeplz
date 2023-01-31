(ns feedme.frontend.core
  (:require [re-frame.core :as rf]
            [reagent.dom :as rdom]
            [goog.dom :as gdom]

            [feedme.frontend.config :as config]
            [feedme.frontend.events :as events]
            [feedme.frontend.view   :as view]))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (rdom/render [view/navbar]
               (gdom/getElement "app")))

(defn ^:export init []
  (rf/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
