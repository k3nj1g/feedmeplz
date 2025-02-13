(ns app.components.card-parts)

(defn card
  [& children]
  (into [:div.bg-white.rounded-xl.border.shadow] children))

(defn card-header
  [& children]
  (into [:div.flex.flex-col.p-6] children))

(defn card-title
  [& children]
  (into [:h3.text-2xl.font-semibold.leading-none.tracking-tight] children))

(defn card-description
  [& children]
  (into [:p.text-gray-500.text-sm] children))

(defn card-content
  [& children]
  (into [:div.p-6.pt-0] children))

