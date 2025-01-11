(ns app.macro)

(defmacro persist-scope
  "Takes local scope vars and defines them in the global scope. Useful for RDD.
                 
                 If given no symbols defs all visible vars."
  ([]
   (let [syms (cond-> &env
                (contains? &env :locals)
                :locals

                :always
                keys)]
     `(persist-scope ~@syms)))
  ([& syms]
   `(do
      ~@(map (fn [sym]
               `(def ~sym ~sym))
             syms))))
