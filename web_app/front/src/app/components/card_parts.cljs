(ns app.components.card-parts)

(defn card
  [& children]
  (into [:div.bg-card.text-card-foreground.rounded-xl.border.shadow.mb-6] children))

(defn card-header
  [props title]
  [:div.p-6
   [:h3.font-semibold.leading-none.tracking-tight
    props
    title]])

(defn card-content
  [& children]
  (into [:div.p-6.pt-0] children))

