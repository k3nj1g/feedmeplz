(ns app.components.utils
  (:require [clojure.string :as str]))

(defn event-value
  [e]
  (.. e -target -value))

(defn make-id
  [path]
  (->> path
       (map name)
       (str/join "-")))
