(ns app.services.helpers)

(defn success
  "Create successful result"
  [data]
  {:success true :data data})

(defn error
  "Create error result"
  ([message]
   {:success false :error message :status 400})
  ([message status]
   {:success false :error message :status status}))

(defn not-found
  "Create not found error"
  []
  (error "Resource not found" 404))

(defn validation-error
  "Create validation error"
  [message]
  (error message 422))
