(ns app.admin.catalog.form)

(def form-path-update [:form :catalog :update])

(def form-schema-update
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
            :weight      {:type  :string
                          :label "Вес"}}})

(def form-path-search [:form :current-menu :search])

(def form-schema-search
  {:type   :form
   :fields {:search {:type :string}}})
