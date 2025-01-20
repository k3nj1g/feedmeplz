(ns app.helpers
  (:require
   [re-frame.core :as rf]))

(def action
  (memoize
   (fn make-handler [event]
     (fn dispatch
       ([]  (rf/dispatch event))
       ([_] (rf/dispatch event))))))
