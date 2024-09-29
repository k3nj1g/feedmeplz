(ns user
  (:require [clojure.tools.namespace.repl :refer [set-refresh-dirs]]
            
            [integrant.repl :refer [clear go halt prep init reset reset-all]]
                        
            [app.config :as config]
            
            [app.core]))

(integrant.repl/set-prep! #(config/prep))

(set-refresh-dirs "src" "resources")
