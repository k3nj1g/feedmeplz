(ns app.admin.daily.import.form)

(def form-path [:form :daily-import])

(def form-schema
  {:type   :form
   :fields {:file {:type       :file
                   :validators {:required {:message "Выберите файл"}}}}})
