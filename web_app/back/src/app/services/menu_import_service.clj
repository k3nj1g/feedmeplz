(ns app.services.menu-import-service
  (:require [app.models.dish :as dish-model]
            [app.models.category :as category-model]
            [app.models.daily-menu :as daily-menu-model]
            [app.models.daily-menu-item :as daily-menu-item-model]
            [app.models.crud :as crud]
            [app.db.core :as db :refer [with-transaction]]
            [app.services.excel-parser :as excel-parser]
            [clojure.string :as str]))

;; ============================================================================
;; Result Helpers
;; ============================================================================

(defn success [data]
  {:success true
   :data data})

(defn error [message & {:keys [status] :or {status 422}}]
  {:success false
   :error message
   :status status})

(defn not-found [message]
  (error message :status 404))

(defn validation-error [message]
  (error message :status 422))

;; ============================================================================
;; Category Name Mapping
;; ============================================================================

(def ^:private category-mapping
  {"Холодные блюда и закуски" "Салаты"
   "Салаты"                   "Салаты"
   "Супы"                     "Супы"
   "Горячие блюда"            "Главные блюда"
   "Гарниры"                  "Гарниры"
   "Десерты"                  "Десерты"
   "Напитки"                  "Напитки"})

;; ============================================================================
;; Helpers
;; ============================================================================

(defn- normalize-string
  "Normalize string for comparison"
  [s]
  (when s
    (-> s
        str/trim
        str/lower-case
        (str/replace #"\s+" " "))))

(defn- load-categories
  "Load all categories from database as map {normalized-name -> id}"
  [db]
  (try
    (let [categories (category-model/find-all db)]
      (into {} (map (fn [cat] [(normalize-string (:name cat)) (:id cat)]) categories)))
    (catch Exception _
      {})))

(defn- map-category-name
  "Map Excel category name to database category name"
  [excel-category]
  (get category-mapping excel-category excel-category))

(defn- resolve-category-id
  "Resolve category ID from Excel category name"
  [categories-map excel-category]
  (when excel-category
    (let [db-category (map-category-name excel-category)
          normalized (normalize-string db-category)]
      (get categories-map normalized))))

(defn- load-dishes
  "Load all dishes from database grouped by normalized name"
  [db]
  (try
    (let [dishes (dish-model/find-all db)]
      (group-by #(normalize-string (:name %)) dishes))
    (catch Exception _
      {})))

(defn- find-matching-dish
  "Find dish by normalized name and category"
  [dishes-map item category-id]
  (let [normalized-name (normalize-string (:name item))
        candidates (get dishes-map normalized-name)]
    (first (filter #(= (:category_id %) category-id) candidates))))

(defn- check-existing-menu
  "Check if menu already exists for given date"
  [datasource date]
  (try
    (let [query {:select [:*]
                 :from [:daily_menus]
                 :where [:= :date date]}
          result (db/execute datasource query)]
      (first result))
    (catch Exception _
      nil)))

(defn- validate-item-data
  "Validate a single item"
  [item row-number]
  (let [errors []]
    (cond-> errors
      (str/blank? (:name item))
      (conj {:row row-number :field "name" :message "Название блюда не может быть пустым"})

      (nil? (:price item))
      (conj {:row row-number :field "price" :message "Цена не указана"})

      (and (:price item) (<= (:price item) 0))
      (conj {:row row-number :field "price" :message "Цена должна быть больше нуля"})

      (str/blank? (:category item))
      (conj {:row row-number :field "category" :message "Категория не указана"}))))

;; ============================================================================
;; File Upload Handling
;; ============================================================================

(defn parse-uploaded-file
  "Parse uploaded Excel file from multipart form data
   
   Accepts file-upload map with either:
   - :stream (stream-based upload)
   - :tempfile (file-based upload from Ring multipart)"
  [file-upload]
  (try
    (if-not file-upload
      (validation-error "Файл не выбран")
      (try
        ;; Get file path - either from :tempfile or create from :stream
        (let [file-path (if (:tempfile file-upload)
                          (.getAbsolutePath (:tempfile file-upload))
                          (let [temp-file (java.io.File/createTempFile "menu_" ".xlsx")]
                            (with-open [in (:stream file-upload)
                                        out (java.io.FileOutputStream. temp-file)]
                              (let [buffer (byte-array 1024)]
                                (loop []
                                  (let [n (.read in buffer)]
                                    (when (> n 0)
                                      (.write out buffer 0 n)
                                      (recur))))))
                            (.getAbsolutePath temp-file)))]
          
          ;; Parse the file
          (success (excel-parser/parse-menu-file file-path)))
        
        (catch Exception e
          (validation-error (str "Ошибка при парсинге файла: " (.getMessage e))))))
    (catch Exception e
      (error (str "Ошибка при обработке файла: " (.getMessage e)) :status 400))))

;; ============================================================================
;; Validation
;; ============================================================================

(defn validate-import
  "Validate parsed menu data.
   
   Returns:
   {:success true
    :data {:status :valid | :invalid
           :date LocalDate
           :summary {:total-dishes int
                     :existing-dishes int
                     :new-dishes int
                     :invalid-dishes int}
           :existing-dishes [{...}]
           :new-dishes [{...}]
           :errors [...]
           :existing-menu? bool}}"
  [db parsed-data]
  (try
    (let [{:keys [date items]} parsed-data
          existing-menu (check-existing-menu db date)
          categories-map (load-categories db)
          dishes-map (load-dishes db)
          
          processed (map-indexed
                     (fn [idx item]
                       (let [row-number (+ idx 10)
                             validation-errors (validate-item-data item row-number)
                             category-id (resolve-category-id categories-map (:category item))
                             matched-dish (when category-id
                                            (find-matching-dish dishes-map item category-id))]
                         {:item item
                          :row row-number
                          :category-id category-id
                          :matched-dish matched-dish
                          :validation-errors (if (nil? category-id)
                                               (conj validation-errors
                                                     {:row row-number
                                                      :field "category"
                                                      :message (str "Категория не найдена: " (:category item))})
                                               validation-errors)}))
                     items)
          
          all-errors (if existing-menu
                       [{:row 0 :field "date" :message (str "Меню на дату " date " уже существует")}]
                       [])
          all-errors (concat all-errors (mapcat :validation-errors processed))
          valid-items (filter #(empty? (:validation-errors %)) processed)
          existing-dishes (filter :matched-dish valid-items)
          new-dishes (remove :matched-dish valid-items)]
      
      (success {:status (if (empty? all-errors) :valid :invalid)
                :date date
                :existing-menu? (boolean existing-menu)
                :summary {:total-dishes (count items)
                          :existing-dishes (count existing-dishes)
                          :new-dishes (count new-dishes)
                          :invalid-dishes (- (count items) (count valid-items))}
                :existing-dishes (map (fn [{:keys [item matched-dish category-id]}]
                                        (assoc item
                                               :dish-id (:id matched-dish)
                                               :category-id category-id
                                               :existing-price (:price matched-dish)))
                                      existing-dishes)
                :new-dishes (map (fn [{:keys [item category-id]}]
                                   (assoc item :category-id category-id))
                                 new-dishes)
                :errors (vec all-errors)}))
    
    (catch Exception e
      (error (str "Ошибка при валидации: " (.getMessage e))))))

;; ============================================================================
;; Import Execution
;; ============================================================================

(defn execute-import
  "Execute import of validated menu.
   
   Parameters:
   - db: database connection
   - validation-result: result from validate-import containing :existing-dishes and :new-dishes
   - date: menu date (LocalDate)
   - create-new-dishes?: whether to create new dishes (default false)
   
   Returns:
   {:success true
    :data {:daily-menu-id int
           :dishes-created int
           :menu-items-created int}}"
  [db {:keys [date existing-dishes new-dishes]} & {:keys [create-new-dishes?]
                                                     :or {create-new-dishes? false}}]
  (try
    (with-transaction [tx db]
      (let [;; 1. Create daily menu
            menu (crud/create! (daily-menu-model/model tx)
                               {:date date
                                :is_published false})
            menu-id (:id menu)
            
            ;; 2. Create new dishes if requested
            created-dishes (if create-new-dishes?
                             (doall (map (fn [dish-data]
                                           (dish-model/create! tx (select-keys dish-data
                                                                               [:name :kcals :weight :price :category_id])))
                                         new-dishes))
                             [])
            
            ;; 3. Build all dish items for menu
            all-dish-items (concat
                            (map (fn [dish]
                                   {:dish-id (:dish-id dish)
                                    :price (:price dish)})
                                 existing-dishes)
                            (map (fn [dish]
                                   {:dish-id (:id dish)
                                    :price (:price dish)})
                                 created-dishes))
            
            ;; 4. Create daily menu items
            menu-items (doall (map (fn [dish-item]
                                     (crud/create! (daily-menu-item-model/model tx)
                                                   {:daily_menu_id menu-id
                                                    :dish_id (:dish-id dish-item)
                                                    :price (:price dish-item)}))
                                   all-dish-items))]
        
        (success {:daily-menu-id menu-id
                  :dishes-created (count created-dishes)
                  :menu-items-created (count menu-items)})))
    
    (catch Exception e
      (error (str "Ошибка при импорте: " (.getMessage e))))))
