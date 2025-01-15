(ns app.components.base)

(defn heading
  [title]
  [:h1.text-2xl.font-semibold title])

(defn button
  [props & children]
  [:button
   (-> props
       (update :class conj :flex :items-center :px-4 :py-2 :bg-blue-500 :text-white :rounded-md :hover:bg-blue-600))
   children])
