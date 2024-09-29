(ns app.config
  (:require [clojure.java.io :as io]

            [aero.core       :as aero]
            [integrant.core  :as ig]))

(defmethod aero/reader 'ig/ref
  [_ _tag value]
  (ig/ref value))

(defmethod aero/reader 'ig/refset
  [_ _tag value]
  (ig/refset value))

(defn read-config
  []
  (-> "config.edn"
      (io/resource)
      (aero/read-config)))

(defn prep
  []
  (let [config (read-config)]
    (ig/load-namespaces config)
    config))
