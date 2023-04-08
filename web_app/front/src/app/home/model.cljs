(ns app.home.model
  (:require [re-frame.core :refer [reg-sub]]
            
            [app.home.controller]))

(reg-sub 
 :home
 (fn [db]
   #_(def d db)
   (->> db :response
        (group-by :category/title)
        (map
         (fn [[category dishes]]
           [category (map (partial reduce-kv
                                   (fn [acc k v]
                                     (assoc acc (keyword (name k)) v))
                                   {})
                          dishes)])))))
