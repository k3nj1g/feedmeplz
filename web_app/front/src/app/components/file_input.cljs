(ns app.components.file-input
  (:require [clojure.string :as str]
            
            ["lucide-react" :refer [Upload File CheckCircle]]
            
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as reagent]
            
            [reagent-mui.material.button :refer [button]]
            [reagent-mui.material.box :refer [box]]
            [reagent-mui.material.typography :refer [typography]]
            [reagent-mui.material.card :refer [card]]
            [reagent-mui.material.card-content :refer [card-content]]
            [reagent-mui.material.chip :refer [chip]]
            
            [app.components.utils :as utils])
  (:require-macros [ps]))

(defn format-file-size
  [bytes]
  (if (or (nil? bytes) (zero? bytes))
    "0 B"
    (let [k 1024
          sizes ["B" "KB" "MB" "GB"]
          i (js/Math.floor (/ (js/Math.log bytes) (js/Math.log k)))]
      (str (.toFixed (/ bytes (js/Math.pow k i)) 2) " " (nth sizes i)))))

(defn get-file-extension
  [filename]
  (when filename
    (let [parts (str/split filename #"\.")]
      (when (> (count parts) 1)
        (str/upper-case (last parts))))))

(defn file-input
  [form-path path & [{:keys [accept multiple props button-text verified?]}]]
  (let [{:keys [errors value]}
        @(subscribe [:zf/node form-path path])

        id
        (utils/make-id path)

        handle-file-change
        (fn [event]
          (let [files         (array-seq (.. event -target -files))
                selected-file (first files)]
            (if multiple
              (dispatch [:zf/set-value form-path path files])
              (dispatch [:zf/set-value form-path path selected-file]))))

        file
        (if multiple
          (when value (first (array-seq value)))
          value)

        file-name
        (when file (.-name file))

        file-size
        (when file (.-size file))

        file-extension
        (get-file-extension file-name)]
    [:div
     [box
      (merge
       {:display        "flex"
        :flex-direction "column"
        :gap            3
        :mb             1}
       props)

      ;; Upload button
      [:div
       [:input
        {:type      "file"
         :id        id
         :accept    accept
         :multiple  multiple
         :disabled  (:disabled props)
         :on-change handle-file-change
         :style     {:display "none"}}]
       [:label {:html-for id}
        [button
         {:component "span"
          :variant   "contained"
          :size      "medium"
          :fullWidth true
          :disabled  (:disabled props)}
         [:> Upload {:class "w-5 h-5 mr-2"}]
         (or button-text "Загрузить файлы")]]]

      ;; File info card
      (when file
        [card
         {:variant "outlined"
          :sx      {:backgroundColor "#ffffff"}}
         [card-content
          [:div.flex.items-center.gap-3
           [:> File {:class "w-5 h-5 text-gray-500"}]
           [:div
            [typography
             {:variant "body1"
              :sx      {:font-weight 500
                        :color       "#1f2937"}}
             file-name]
            [typography
             {:variant "caption"
              :sx      {:color "#6b7280"}}
             (str (format-file-size file-size) " | " file-extension)]]]
          (when verified?
            [chip
             {:icon    (reagent/as-element [:> CheckCircle {:class "w-4 h-4"}])
              :label   "Проверено"
              :color   "success"
              :size    "small"}])]])

     (when (seq errors)
       [typography
        {:variant   "caption"
         :color     "error"
         :component "div"}
        (str/join ", " (vals errors))])]]))
