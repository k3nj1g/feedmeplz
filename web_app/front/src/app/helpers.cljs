(ns app.helpers
  (:require [clojure.string :as str]
            
            [re-frame.core :as rf]))

(def action
  (memoize
   (fn make-handler [event]
     (fn dispatch
       ([]  (rf/dispatch event))
       ([_] (rf/dispatch event))))))

(defn vectorize
  [x]
  (cond
    (nil? x)
    (vector)

    (sequential? x)
    (vec x)

    :else
    (vector x)))

(defn in?
  [elm coll]
  (boolean (some #{elm} coll)))

(defn success-event-to-dispatch
  [success data]
  [(:event success) (assoc (:params success) :data data)])

(defn success-event
  ([success data]
   (assoc-in success [:params :data] data)))

(defn match-search-term?
  [in search]
  (every?
   #(str/includes? (str/lower-case in) %)
   (some-> search (str/lower-case) (str/split #"\s+"))))
