(ns app.handler
  (:require [ring.util.response :refer [response]]
            [ring.middleware.json :refer [wrap-json-response]]
            [honey.sql :as hsql]
            [next.jdbc.sql :as sql]))

(defn get-menu [datasource]
  (fn [_]
    (let [data (sql/query datasource
                          (hsql/format {:select [:d.id :d.name :d.description :d.price
                                                 [:c.name :category]]
                                        :from [[:dishes :d]]
                                        :join [[:categories :c] [:= :d.category_id :c.id]]
                                        :order-by [[:c.name :asc] [:d.name :asc]]}))]
      {:status 200
       :body data})))
