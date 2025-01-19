(ns app.core
  (:require [re-frame.core :as rf]
            [reagent.dom :as rdom]
            [goog.dom :as gdom]

            [zenform.core]

            [app.config :as config]
            [app.events :as events]
            [app.view   :as view]
            [app.routes :as routes]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (let [root-el (gdom/getElement "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [view/layout-view] root-el)))

(defn ^:export init []
  (routes/start!)
  (rf/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
