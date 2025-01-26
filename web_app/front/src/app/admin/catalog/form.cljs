(ns app.admin.catalog.form)

(def form-path [:form :catalog])

(def form-schema
  {:type   :form
   :fields {:name        {:type       :string
                          :label      "Название"
                          :validators {:required {:message "Укажите название"}}}
            :description {:type  :string
                          :label "Описание"}
            :price       {:type       :number
                          :label      "Цена"
                          :validators {:required {:message "Укажите цену"}}}
            :category_id {:type  :number
                          :label "Категория"}
            :kcals       {:type  :number
                          :label "Калории"}
            :weight      {:type  :number
                          :label "Вес"}}})
