(ns app.routes
  "Namespace for handling application routing"
  (:require [bidi.bidi     :as bidi]
            [pushy.core    :as pushy]
            [re-frame.core :as rf]

            [app.events :as events]))

(defmulti pages identity)
(defmethod pages :default [] (fn [] [:div "Страница не существует по заданному пути."]))

;; Route definitions
(def routes
  "Application routes"
  ["/" {""       :current-menu
        "login"  :login
        "admin/" {"catalog"     :admin-catalog
                  "daily-list"  :admin-daily-list
                  "daily-crud/" {""    :admin-daily-create
                                 [:id] :admin-daily-update}}}])

;; URL parsing and generation
(defn parse-url
  "Parse a URL and return the corresponding route"
  [url]
  (bidi/match-route routes url))

(defn url-for
  "Generate a URL for a given route"
  [handler]
  (apply bidi/path-for routes handler))

;; Navigation handling
(defn dispatch
  "Dispatch a route to the appropriate handler"
  [route]
  (rf/dispatch [::events/set-active-page
                {:page         (:handler route)
                 :route-params (:route-params route)}])
  (rf/dispatch [:db/set :route-params (:route-params route)]))

(def history
  "Pushy history object for handling browser history"
  (pushy/pushy dispatch parse-url))

(defn navigate!
  "Navigate to a new route"
  [handler]
  (pushy/set-token! history (url-for handler)))

(defn start!
  "Start the routing system"
  []
  (pushy/start! history))

;; Re-frame effects
(rf/reg-fx
 :navigate
 (fn [handler]
   (navigate! handler)))
