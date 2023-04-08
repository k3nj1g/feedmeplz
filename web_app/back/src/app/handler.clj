(ns app.handler
  (:require [ring.util.response :refer [response]]
            [ring.middleware.json :refer [wrap-json-response]]
            [gungnir.query :as query]
            [gungnir.database  :as gd]
            [honey.sql :as hsql]
            [next.jdbc.sql :as sql]))

(defn get-menu [request]
  (let [data (sql/query gd/*datasource* 
                        (hsql/format {:select [:*]
                                      :from   [:menu]
                                      :join   [[:category]
                                               [:= :category.id :menu.fk_category]]}))] 
    {:status 200
     :body   data}))

(def app (wrap-json-response get-menu))
