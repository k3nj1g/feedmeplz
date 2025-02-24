(ns app.helpers
  (:require [malli.core  :as m]
            [malli.error :as me]
            [malli.experimental.time :as met]
            [malli.registry :as mr]))

(mr/set-default-registry!
 (mr/composite-registry
  (m/default-schemas)
  (met/schemas)))

(defn validate-data [schema data]
  (let [result (m/validate schema data)]
    (if (true? result)
      nil
      (me/humanize (m/explain schema data)))))

(defn parse-int
  [number-string]
  (try (Integer/parseInt number-string)
       (catch Exception _ nil)))

(defn as-int
  [value]
  (when value
    (try
      (cond
        (number? value) (int value)
        (string? value) (parse-int value)
        :else (throw (IllegalArgumentException. "Input must be a number or a string")))
      (catch NumberFormatException e
        (throw (ex-info "Failed to parse value to int"
                        {:input value
                         :error (.getMessage e)})))
      (catch Exception e
        (throw (ex-info "Unexpected error while converting to int"
                        {:input value
                         :error (.getMessage e)}))))))

(defn as-double
  [value]
  (when value
   (try
     (cond
       (number? value) (double value)
       (string? value) (parse-double value)
       :else (throw (IllegalArgumentException. "Input must be a number or a string")))
     (catch NumberFormatException e
       (throw (ex-info "Failed to parse value to double"
                       {:input value
                        :error (.getMessage e)})))
     (catch Exception e
       (throw (ex-info "Unexpected error while converting to double"
                       {:input value
                        :error (.getMessage e)}))))))
