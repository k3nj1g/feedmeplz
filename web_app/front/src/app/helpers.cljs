(ns app.helpers
  (:require
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
