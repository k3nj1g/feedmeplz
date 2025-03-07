(ns app.handlers.category-handler 
  (:require [ring.util.response :as response]

            [app.server.db :refer [execute-query]]))

(defn dishes-by-category
  [datasource]
  (fn [request]
    (let [category-id (get-in request [:path-params :category_id])
          dishes      (execute-query datasource
                                     {:select   [:*]
                                      :from     [:dishes]
                                      :where    [:= :category_id [:cast category-id :integer]]
                                      :order-by [[:name :asc]]})]
      (response/response dishes))))
