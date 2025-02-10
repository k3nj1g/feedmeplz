(ns app.macro)

(defmacro persist-scope
  "Takes local scope vars and defines them in the global scope. Useful for RDD.
                 
                 If given no symbols defs all visible vars."
  ([]
   (let [symbols (cond-> &env
                   (contains? &env :locals)
                   :locals

                   :always
                   keys)]
     `(persist-scope ~@symbols)))
  ([& symbols]
   `(do
      ~@(map (fn [symbol]
               `(def ~symbol ~symbol))
             symbols))))
