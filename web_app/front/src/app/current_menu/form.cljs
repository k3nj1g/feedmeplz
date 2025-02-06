(ns app.current-menu.form)

(def form-path [:form :current-menu])

(def form-schema
  {:type   :form
   :fields {:search {:type :string}}})
