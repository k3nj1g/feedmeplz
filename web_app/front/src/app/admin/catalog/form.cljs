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
                          :adornment  "₽"
                          :validators {:required {:message "Укажите цену"}}}
            :category_id {:type  :number
                          :label "Категория"}
            :kcals       {:type      :number
                          :label     "Калории"
                          :adornment "ккал"}
            :weight      {:type      :number
                          :label     "Вес"
                          :adornment "г"}}})
