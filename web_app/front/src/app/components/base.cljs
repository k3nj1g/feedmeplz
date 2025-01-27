(ns app.components.base
  (:require [reagent-mui.material.button :refer [button] :rename {button mui-button}]))

(defn heading
  [title]
  [:h1.text-2xl.font-semibold title])

(defn button
  [props & children]
  (into
   [mui-button
    (merge props (case (:type props)
                   "primary" (merge {:variant "contained"})
                   "success" (merge {:variant "contained" :color "success"})
                   "danger"  (merge {:variant "contained" :color "error"})
                   {:variant "text"}))]
   children))
