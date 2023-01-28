(ns build
  "Build jar with
     clj -T:build ci"
  (:refer-clojure :exclude [test])
  (:require [org.corfield.build :as bb]))

(def lib 'feed-me-plz/back)
(def version "0.1.0-SNAPSHOT")
(def main 'app.core)

(defn test "Run the tests." [opts]
  (bb/run-tests opts))

(defn ci "Run the CI pipeline of tests (and build the uberjar)." [opts]
  (-> opts
      (assoc :lib lib :version version :main main)
      (bb/run-tests)
      (bb/clean)
      (bb/uber)))