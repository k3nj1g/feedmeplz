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

(defn get-categories [datasource]
  (fn [_]
    (let [data (sql/query datasource
                          (hsql/format {:select [:*]
                                        :from [:categories]
                                        :order-by [[:name :asc]]}))]
      {:status 200
       :body data})))

(defn add-dish [datasource]
  (fn [request]
    (let [dish (:body request)
          result (sql/insert! datasource :dishes dish)]
      {:status 201
       :body result})))

(defn add-category [datasource]
  (fn [request]
    (let [category (:body request)
          result (sql/insert! datasource :categories category)]
      {:status 201
       :body result})))
