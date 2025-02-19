(ns build
  "Build jar with
     clj -T:build ci"
  (:refer-clojure :exclude [test])
  (:require [org.corfield.build :as bb]))

(def lib 'feed-me-plz/back)
(def version "0.1.0")
(def main 'app.core)

(defn write-version [opts]
  (spit "resources/version.txt" version)
  opts)

(defn release
  [opts]
  (-> opts
      (assoc :lib lib :version version :main main)
      (write-version)
      (bb/clean)
      (bb/uber)))

(defn cli
  [opts]
  (-> opts
      (assoc :lib lib :version "cli" :main 'app.cli)
      (write-version)
      (bb/clean)
      (bb/uber)))
