(ns app.server.telegram
  (:require [clojure.java.io    :as io]
            [clojure.string     :as str]
            [clojure.core.async :as async]

            [clj-fuzzy.metrics    :as fuzzy]
            [integrant.core       :as ig]
            [telegrambot-lib.core :as tbot] 
            
            [app.models.dish :as dish]
            [app.models.crud :as crud] 
            [app.server.tesseract :as tesseract] 

            [app.models.category :as category]
            [app.models.daily-menu :as daily-menu]
            [app.models.daily-menu-item :as daily-menu-item]))

(defn save-photo [uri file]
  (try
    (let [file-path (str "resources/images/" file)]
      (.mkdirs (io/file "resources/images"))
      (with-open [in  (io/input-stream uri)
                  out (io/output-stream file-path)]
        (io/copy in out))
      (prn "file-path" file-path)
      file-path)
    (catch Exception e
      (println "Error saving photo:" (.getMessage e)))))

(defn generate-unique-filename [extension]
  (str (java.util.UUID/randomUUID) "." extension))

(defn from-menu-sender?
  [menu-sender-id {{:keys [id]} :from
                   :as          message}]
  (when (= (str id)
           (str menu-sender-id))
    message))

(defn get-photo-from-message
  [message bot token menu-sender-id]
  (let [file-url  (some->> message
                           :message
                           (from-menu-sender? menu-sender-id)
                           :photo
                           (last)
                           :file_id
                           (tbot/get-file bot)
                           :result :file_path
                           (str "https://api.telegram.org/file/bot" token "/"))
        file-name (some-> file-url
                          (str/last-index-of ".")
                          (inc)
                          (->> (subs file-url))
                          (generate-unique-filename))]
    (when file-name
      (save-photo file-url file-name))))

(defmethod ig/init-key :persistent/telegram
  [_ {:keys [token menu-sender-id datasource]}] 
  (let [oni-chan (async/chan)
        bot      (tbot/create {:bot-token token})]
    (async/go-loop
     [offset nil]
      (let [updates    (tbot/get-updates bot {:offset offset :timeout 5})
            new-offset (when-let [last-update (last (:result updates))]
                         (inc (:update_id last-update)))]
        (doseq [update (:result updates)]
          (some->> (get-photo-from-message update bot token menu-sender-id)
                   (async/>! oni-chan)))
        (recur new-offset)))
    (async/go-loop
     []
      (when-let [file-path (async/<! oni-chan)]
        (try
          (let [file (io/file file-path)
                text (tesseract/get-file-text file)
                menu (tesseract/parse-menu text)

                dish-list     (crud/list-all (dish/model datasource) {})
                category-list (crud/list-all (category/model datasource) {})
                category'     (do (->> menu
                                       (group-by :category)
                                       (keys)
                                       (remove (fn [elem]
                                                 (->> category-list
                                                      (map (fn [{:keys [name]}]
                                                             (fuzzy/dice elem name)))
                                                      (sort >)
                                                      (first)
                                                      (< 0.8))))
                                       (map (fn [name]
                                              {:name        (str/capitalize name)
                                               :description "create by tesseract"}))
                                       (map (partial crud/create! (category/model datasource))))
                                  (crud/list-all (category/model datasource) {}))
                dish-list'    (do (->> menu
                                       (map (fn [{:keys [category] :as elem}]
                                              (let [category-id (->> category'
                                                                     (map (fn [{:keys [name id]}]
                                                                            [(fuzzy/dice category name)
                                                                             id]))
                                                                     (sort-by first >)
                                                                     (first)
                                                                     (last))]
                                                (-> (select-keys elem [:name :price])
                                                    (assoc :category_id category-id)))))
                                       (remove (fn [{menu-dish-name :name}]
                                                 (->> dish-list
                                                      (map (fn [{:keys [name]}]
                                                             (fuzzy/dice menu-dish-name name)))
                                                      (sort >)
                                                      (first)
                                                      (< 0.8))))
                                       (mapv #(assoc % :description "create by tesseract"))
                                       (mapv #(update % :name str/capitalize))
                                       (mapv (partial crud/create! (dish/model datasource))))
                                  (crud/list-all (dish/model datasource) {}))
                daily-menu    (crud/create! (daily-menu/model datasource) {:date (java.time.LocalDate/now)})]
            (->> menu
                 (mapv (fn [{menu-dish-name :name}]
                         (->> dish-list'
                              (map (fn [{:keys [name] :as dish}]
                                     [(fuzzy/dice menu-dish-name name)
                                      dish]))
                              (sort-by first >)
                              (first)
                              (last))))
                 (mapv (fn [{:keys [id price]}]
                         {:daily_menu_id (:id daily-menu)
                          :dish_id       id
                          :price         (double price)}))
                 (mapv (partial crud/create! (daily-menu-item/model datasource))))
            (if (.delete file)
              (println (str "Deleted " file-path))
              (println (str "Failed to delete " file-path))))
          (catch Exception e
            (println "Error deleting photo:" (.getMessage e)))))
      (recur))))


