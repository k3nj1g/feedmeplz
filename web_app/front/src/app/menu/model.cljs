(ns app.menu.model
  (:require [re-frame.core :refer [reg-sub]]
            
            [app.menu.controller]))

(reg-sub 
 :home
 (fn [db]
   (->> db :http/response :menu
        (group-by :category/title)
        (map
         (fn [[category dishes]]
           [category (map (partial reduce-kv
                                   (fn [acc k v]
                                     (assoc acc (keyword (name k)) v))
                                   {})
                          dishes)])))))
