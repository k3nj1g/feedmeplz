(ns app.admin.daily.crud.form)

(def form-path [:form :daily])

(defn category->path
  [category]
  (keyword (str "category-" (:id category))))

(defn form-schema
  [categories]
  {:type   :form
   :fields (merge
            {:date {:type :string}}
            (reduce
             (fn [acc category]
               (assoc acc (category->path category) {:type   :form
                                                     :fields {:search {:type :string}
                                                              :dishes {:type :vector}}}))
             {}
             categories))})

(def form-path-update [:form :dish])

(def form-schema-update
  {:type   :form
   :fields {:category_id {:type :number}
            :name        {:type       :string
                          :label      "Название"
                          :validators {:required {:message "Укажите название"}}}
            :price       {:type       :number
                          :label      "Цена"
                          :validators {:required {:message "Укажите цену"}}}
            :kcals       {:type  :number
                          :label "Калории"}
            :weight      {:type  :number
                          :label "Вес"}}})
