(ns app.routes
  (:require [bidi.bidi     :as bidi]
            [pushy.core    :as pushy]
            [re-frame.core :as rf]
            
            [app.events :as events]))

(defmulti pages identity)

(defmethod pages :default [] [:div "Страница не существует по заданному пути."])

(def routes
  (atom
   ["/" {""      :home
         "about" :about}]))

(defn parse-url
  [url]
  (bidi/match-route @routes url))

(defn url-for
  [& args]
  (apply bidi/path-for (into [@routes] args)))

(defn dispatch
  [route]
  (rf/dispatch [::events/set-active-page {:page         (:handler route)
                                          :route-params (:route-params route)}]))

(def history
  (pushy/pushy dispatch parse-url))

(defn navigate!
  [handler]
  (pushy/set-token! history (url-for handler)))

(defn start!
  []
  (pushy/start! history))

(rf/reg-fx
 :navigate
 (fn [handler]
   (navigate! handler)))
