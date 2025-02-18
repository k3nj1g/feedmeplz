(ns app.login.form)

(def form-path [:form :login])

(def form-schema
    {:type   :form
     :fields {:username {:type       :string
                         :label      "Имя пользователя"
                         :validators {:required {:message "Укажите имя пользователя"}}}
              :password {:type       :string
                         :label      "Пароль"
                         :validators {:required {:message "Укажите пароль"}}}}})
