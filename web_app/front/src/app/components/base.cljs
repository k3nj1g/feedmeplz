(ns app.components.base
  (:require [reagent-mui.material.button :refer [button] :rename {button mui-button}]
            
            [reagent-mui.styles :as styles]

            [app.helpers :as h]))

(defn custom-styles
  []
  {})

(defn heading
  [title]
  [:h1.text-2xl.font-semibold title])

(def styled-button (styles/styled mui-button custom-styles))

(defn button
  [props & children]
  (into
   [styled-button
    (merge
     (update props :class (comp #(conj % "h-10") h/vectorize))
     (case (:type props)
       "primary" (merge {:variant "contained"})
       "success" (merge {:variant "contained" :color "success"})
       "danger"  (merge {:variant "contained" :color "error"})
       "default" (merge {:variant "outlined"})
       {:variant "text"}))]
   children))
