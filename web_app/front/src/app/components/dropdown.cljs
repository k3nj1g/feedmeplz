(ns app.components.dropdown
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [goog.events :as events]))

(defn dropdown
  [{:keys [popup-id]}]
  (let [open?                (rf/subscribe [:active-popup-menu popup-id])
        wrapper-ref          (r/atom nil)
        toggle-dropdown      #(rf/dispatch [:toggle-popup-menu popup-id])
        close-dropdown       #(rf/dispatch [:close-popup-menu popup-id])
        handle-click-outside (fn [event]
                               (when (and @wrapper-ref
                                          (not (.contains @wrapper-ref (.-target event))))
                                 (close-dropdown)))]
    (r/create-class
     {:component-did-mount
      (fn []
        (events/listen js/document "mousedown" handle-click-outside))

      :component-will-unmount
      (fn []
        (events/unlisten js/document "mousedown" handle-click-outside))

      :reagent-render
      (fn [{:keys [trigger content placement]}]
        [:div.relative.inline-flex
         {:ref #(reset! wrapper-ref %)}
         [(:tag trigger)
          (-> (:props trigger)
              (update :class conj :relative)
              (assoc :on-click toggle-dropdown)) 
          (:content trigger)]
         (when @open?
           [:div.absolute.z-10.mt-2.w-56.rounded-md.shadow-lg.bg-white.ring-1.ring-black.ring-opacity-5
            {:class (case placement
                      :top :top-full)}
            content])])})))
