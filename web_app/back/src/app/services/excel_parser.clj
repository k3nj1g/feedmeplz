(ns app.services.excel-parser
  (:require [dk.ative.docjure.spreadsheet :as excel]
            [clojure.string :as str]
            [java-time.api :as jt])
  (:import [java.io File]))

(defn- cell-value
  "Safely get cell value as string or number"
  [cell]
  (when cell
    (try
      (excel/read-cell cell)
      (catch Exception _ nil))))

(defn- parse-date-from-sheet-name
  "Parse date from sheet name format: DD.MM.YY -> LocalDate"
  [sheet-name]
  (try
    (let [[day month year] (str/split sheet-name #"\.")]
      (jt/local-date (+ 2000 (Integer/parseInt year))
                     (Integer/parseInt month)
                     (Integer/parseInt day)))
    (catch Exception e
      (throw (ex-info "Invalid sheet name format. Expected DD.MM.YY"
                      {:sheet-name sheet-name
                       :error (.getMessage e)})))))

(defn- parse-price
  "Parse price from various formats: number, string like '15.00'"
  [value]
  (when value
    (try
      (cond
        (number? value) (double value)
        (string? value) (Double/parseDouble (str/replace value #"," "."))
        :else nil)
      (catch Exception _ nil))))

(defn- parse-number
  "Parse number from cell value"
  [value]
  (when value
    (try
      (cond
        (number? value) value
        (string? value) (when-not (or (str/blank? value) (= "-" value))
                          (Double/parseDouble (str/replace value #"," ".")))
        :else nil)
      (catch Exception _ nil))))

(defn- category-row?
  "Check if row is a category header.
   Category rows have text in column B but no price (price is the key indicator).
   Categories are typically: 'Салаты', 'Супы', 'Горячие блюда', etc."
  [row-data]
  (let [{:keys [name price]} row-data
        name-str (str/trim (or name ""))
        ;; Common category patterns
        category-patterns #{"салаты" "супы" "горячие блюда" "холодные блюда" 
                           "закуски" "гарниры" "десерты" "напитки"
                           "холодные блюда и закуски"}]
    (and (not (str/blank? name-str))
         (nil? price)  ; Categories don't have prices - this is the key indicator
         ;; Check if it matches known category patterns (case-insensitive)
         (or (contains? category-patterns (str/lower-case name-str))
             ;; Or if it's a longer text without price and doesn't look like a dish name
             (and (> (count name-str) 3)
                  (not (re-matches #"^[№\s\d]+$" name-str))
                  ;; Not a typical dish name pattern (dish names often have numbers or are shorter)
                  (not (re-matches #"^\d+[\.\)]\s*" name-str)))))))

(defn- should-skip-row?
  "Check if row should be skipped (empty, header, metadata).
   Don't skip category rows - they are handled separately."
  [row-data]
  (let [{:keys [name price]} row-data
        name-str (str/trim (or name ""))]
    (or (str/blank? name-str)
        ;; Skip header rows with column names
        (re-matches #"(?i).*(меню|выход|цена|пищевая ценность|наименование|калории|белки|жиры|углеводы).*" name-str)
        ;; Skip rows that look like headers (all caps, short)
        (and (< (count name-str) 10)
             (= name-str (str/upper-case name-str))))))

(defn- parse-row
  "Parse a single row into dish data structure.
   Tries to find price in common columns (H=7, I=8, J=9)."
  [row]
  (let [cells (vec (excel/cell-seq row))
        get-cell (fn [idx] (when (< idx (count cells)) (cell-value (nth cells idx))))
        ;; Try to find price in columns H, I, or J
        price (or (parse-price (get-cell 7))  ; Column H
                  (parse-price (get-cell 8))  ; Column I
                  (parse-price (get-cell 9)))] ; Column J
    {:recipe-number (get-cell 0)  ; Column A
     :name          (str/trim (or (get-cell 1) ""))  ; Column B
     :kcals         (parse-number (get-cell 2))  ; Column C
     :proteins      (parse-number (get-cell 3))  ; Column D
     :fats          (parse-number (get-cell 4))  ; Column E
     :carbs         (parse-number (get-cell 5))  ; Column F
     :weight        (str (or (get-cell 6) ""))   ; Column G - can be "250/20" etc
     :price         price}))

(defn- extract-dishes
  "Extract dishes from sheet rows, grouping by category"
  [sheet]
  (let [all-rows (vec (excel/row-seq sheet))
        rows-to-process (drop 9 all-rows)  ; Skip first 9 rows (headers)
        debug-info (atom {:categories-found 0
                          :dishes-found 0
                          :rows-processed 0
                          :rows-skipped 0})
        result (loop [rows rows-to-process
                      current-category nil
                      result []]
                 (if (empty? rows)
                   result
                   (let [row-data (parse-row (first rows))]
                     (swap! debug-info update :rows-processed inc)
                     (cond
                       ;; Found a category header - set it as current category
                       (category-row? row-data)
                       (do
                         (swap! debug-info update :categories-found inc)
                         (recur (rest rows)
                                (str/trim (:name row-data))
                                result))
                       
                       ;; Skip invalid/empty rows (but not if we're looking for categories)
                       (should-skip-row? row-data)
                       (do
                         (swap! debug-info update :rows-skipped inc)
                         (recur (rest rows)
                                current-category
                                result))
                       
                       ;; Valid dish row - must have category and price
                       (and current-category
                            (not (str/blank? (:name row-data)))
                            (:price row-data))
                       (do
                         (swap! debug-info update :dishes-found inc)
                         (recur (rest rows)
                                current-category
                                (conj result (assoc row-data :category current-category))))
                       
                       ;; Row with data but no category - skip (we haven't found a category yet)
                       (and (not (str/blank? (:name row-data)))
                            (:price row-data)
                            (nil? current-category))
                       (recur (rest rows)
                              current-category
                              result)
                       
                       ;; Unknown row type - skip
                       :else
                       (recur (rest rows)
                              current-category
                              result)))))]
    ;; Log debug info if no dishes found
    (when (empty? result)
      (println (format "[DEBUG extract-dishes] No dishes extracted. %s" @debug-info)))
    result))

(defn parse-menu-file
  "Parse Excel menu file and return structured data.

   Returns:
   {:date LocalDate
    :items [{:category str
             :name str
             :kcals number (optional)
             :proteins number (optional)
             :fats number (optional)
             :carbs number (optional)
             :weight str (optional)
             :price number}]}"
  [file-path]
  (try
    (let [workbook (excel/load-workbook file-path)
          sheet (first (excel/sheet-seq workbook))
          sheet-name (excel/sheet-name sheet)
          menu-date (parse-date-from-sheet-name sheet-name)
          dishes (extract-dishes sheet)
          total-rows (count (vec (excel/row-seq sheet)))]
      ;; Debug info: if no dishes found, log some diagnostic info
      (when (empty? dishes)
        (let [sample-rows (take 20 (drop 9 (excel/row-seq sheet)))
              sample-data (map parse-row sample-rows)
              categories-found (filter category-row? sample-data)
              rows-with-price (filter #(and (:price %) (not (str/blank? (:name %)))) sample-data)]
          (println (format "[DEBUG] No dishes found. Sheet: %s, Total rows: %d, Sample rows: %d, Categories in sample: %d, Rows with price: %d"
                          sheet-name total-rows (count sample-rows) (count categories-found) (count rows-with-price)))))
      {:date menu-date
       :items dishes})
    (catch Exception e
      (throw (ex-info "Failed to parse Excel file"
                      {:file-path file-path
                       :error (.getMessage e)}
                      e)))))

(comment
  ;; Test parsing
  (def result (parse-menu-file "/home/d-nibaev/work/feedmeplz/menu.xlsx"))

  (println "Date:" (:date result))
  (println "Total dishes:" (count (:items result)))
  (println "\nFirst few dishes:")
  (doseq [dish (take 5 (:items result))]
    (println (select-keys dish [:category :name :price])))
  
  ;; Debug: inspect raw sheet data
  (let [workbook (excel/load-workbook "/home/d-nibaev/work/feedmeplz/menu.xlsx")
        sheet (first (excel/sheet-seq workbook))
        sheet-name (excel/sheet-name sheet)
        all-rows (vec (excel/row-seq sheet))]
    (println "\n=== DEBUG INFO ===")
    (println "Sheet name:" sheet-name)
    (println "Total rows in sheet:" (count all-rows))
    (println "\nFirst 15 rows (raw):")
    (doseq [[idx row] (map-indexed vector (take 15 all-rows))]
      (let [cells (vec (excel/cell-seq row))
            row-data (parse-row row)]
        (println (format "Row %d: cells=%d, parsed=%s" 
                        idx 
                        (count cells)
                        (select-keys row-data [:name :price :kcals :category])))))
    (println "\nRows after skipping 9:")
    (doseq [[idx row] (map-indexed vector (take 20 (drop 9 all-rows)))]
      (let [row-data (parse-row row)
            is-category? (category-row? row-data)
            should-skip? (should-skip-row? row-data)]
        (println (format "Row %d (actual %d): name=%s, price=%s, kcals=%s, is-category?=%s, should-skip?=%s"
                        idx
                        (+ idx 9)
                        (:name row-data)
                        (:price row-data)
                        (:kcals row-data)
                        is-category?
                        should-skip?))))))
