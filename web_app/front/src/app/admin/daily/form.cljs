(ns app.admin.daily.form)

(def form-path [:form :daily])

(defn category->path
  [category]
  (keyword (str "category-" (:id category))))

(defn form-schema
  [categories]
  {:type   :form
   :fields (reduce 
            (fn [acc category]
              (assoc acc (category->path category) {:type   :form
                                                    :fields {:search {:type :string}
                                                             :dishes {:type :vector}}}))
            {}
            categories)})

