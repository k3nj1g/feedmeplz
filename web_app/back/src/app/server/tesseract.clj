(ns app.server.tesseract
  (:require [clojure.string :as str])
  (:import (net.sourceforge.tess4j Tesseract)))

(def tesseract (new Tesseract))

(do (.setDatapath tesseract "resources/tessdata")
    (.setLanguage tesseract "rus")
    (.setVariable tesseract "user_defined_dpi" "300")
    (.setTessVariable tesseract "tessedit_pageseg_mode", "6")
    (.setTessVariable tesseract "textord_heavy_nr" "1")
    (.setOcrEngineMode tesseract 1))

(defn get-file-text
  [file]
  (->> file
       (.doOCR tesseract)
       (str/lower-case)))

(defn parse-line [line]
  (let [standard-pattern    #"(?<name>.+?)\s+(?<weight>\d+)гр(?:/(?<kcal>\d+)ккал)?\s+(?<price>\d+)\s*руб\.?"
        special-pattern     #"(?<name>.+?)\s+(?<weight>\d+)гр/(?<extra>.+?)\s+(?<price1>\d+)\s*руб\./(?<price2>\d+)\s*руб\.?"
        alternative-pattern #"(?<name>.+?)\s+(?<count>\d+)\s*кус\.?\s+(?<price>\d+)\s*руб\.?"
        match-standard      (re-matcher standard-pattern line)
        match-special       (re-matcher special-pattern line)
        match-alternative   (re-matcher alternative-pattern line)]
    (cond
      ;; Обычный случай
      (.find match-standard)
      {:name   (str/trim (.group match-standard "name"))
       #_#_:weight (Integer/parseInt (.group match-standard "weight"))
       #_#_:kcals  (when-let [kcal (.group match-standard "kcal")]
                 (Integer/parseInt kcal))
       :price  (Integer/parseInt (.group match-standard "price"))}

      ;; Случай с двумя ценами
      (.find match-special)
      {:name   (str/trim (.group match-special "name"))
       #_#_:weight (Integer/parseInt (.group match-special "weight"))
       :price  (Integer/parseInt (.group match-special "price2"))
       #_#_:доп    (str/trim (.group match-special "extra"))
       #_#_:цены   [(Integer/parseInt (.group match-special "price1"))
                    (Integer/parseInt (.group match-special "price2"))]}

      (.find match-alternative)
      {:name       (str/trim (.group match-alternative "name"))
       #_#_:количество (Integer/parseInt (.group match-alternative "count"))
       :price      (Integer/parseInt (.group match-alternative "price"))})))

(defn parse-menu [text]
  (let [lines           (str/split-lines text)
        sections        #{"салаты" "cуп" "второе" "гарниры"}
        result          (atom [])
        current-section (atom nil)]
    (doseq [line lines]
      (let [trimmed (str/trim line)]
        (cond
          (sections trimmed)
          (reset! current-section trimmed)
          (and @current-section (not (str/blank? trimmed)))
          (when-let [parsed (parse-line trimmed)]
            (swap! result conj (assoc parsed :category @current-section))))))
    @result))
