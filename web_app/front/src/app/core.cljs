(ns app.core
  (:require [re-frame.core :as rf]
            [reagent.dom :as rdom]
            [goog.dom :as gdom]

            [app.config :as config]
            [app.events :as events]
            [app.view   :as view]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (let [root-el (gdom/getElement "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [view/navbar] root-el)))

(defn init []
  (rf/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
