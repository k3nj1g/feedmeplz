(ns app.admin.daily.import.controller
  (:require [re-frame.core :refer [reg-event-fx reg-event-db]]
            
            [zenform.model :as zf]
            
            [app.admin.daily.import.form :as form]))

;; Бизнес-логика хранится под :admin-daily-import в app-db
(def business-logic-path [:admin-daily-import])

;; Initialize import page
(reg-event-fx
 :admin-daily-import
 (fn [_ _]
   {:dispatch [:zf/init form/form-path form/form-schema {}]}))

;; Initialize state for reset
(reg-event-db
 :admin-daily-import/init-state
 (fn [db _]
   (assoc-in db business-logic-path
             {:status             :idle
              :validation-result  nil
              :create-new-dishes? true
              :error              nil})))

;; Toggle create new dishes
(reg-event-db
 :admin-daily-import/toggle-create-new-dishes
 (fn [db _]
   (update-in db (conj business-logic-path :create-new-dishes?) not)))

;; Upload and validate file
(reg-event-fx
 :admin-daily-import/validate-file
 (fn [{db :db} [_ file]]
   (if-not file
     {:db (assoc-in db (conj business-logic-path :error) "Пожалуйста, выберите файл")}
     (let [form-data (doto (js/FormData.)
                       (.append "file" file))]
       (print "Validating file:" file form-data)
       {:db           (assoc-in db (conj business-logic-path :status) :validating)
        :http/request {:method  :post
                       :uri     "/api/daily-menus/import/validate"
                       :body    form-data
                       :pid     :admin-daily-import/validate
                       :success {:event :admin-daily-import/validation-success}
                       :failure {:event :admin-daily-import/validation-failure}}}))))

;; Validation success
(reg-event-db
 :admin-daily-import/validation-success
 (fn [db [_ response]]
   (-> db
       (assoc-in (conj business-logic-path :status) :validated)
       (assoc-in (conj business-logic-path :validation-result) response)
       (assoc-in (conj business-logic-path :error) nil))))

;; Validation failure
(reg-event-db
 :admin-daily-import/validation-failure
 (fn [db [_ error]]
   (-> db
       (assoc-in (conj business-logic-path :status) :error)
       (assoc-in (conj business-logic-path :error) (or (:message error)
                                                        (:error error)
                                                        "Ошибка валидации файла")))))

;; Execute import
(reg-event-fx
 :admin-daily-import/execute
 (fn [{db :db} _]
   (let [validation-result (get-in db (conj business-logic-path :validation-result))
         create-new-dishes? (get-in db (conj business-logic-path :create-new-dishes?))]
     (if-not validation-result
       {:db (assoc-in db (conj business-logic-path :error) "Нет данных для импорта")}
       {:db (assoc-in db (conj business-logic-path :status) :importing)
        :http/request {:method :post
                       :uri "/api/daily-menus/import/execute"
                       :body {:validation-result validation-result
                              :create-new-dishes create-new-dishes?}
                       :pid ::execute
                       :success {:event :admin-daily-import/import-success}
                       :failure {:event :admin-daily-import/import-failure}}}))))

;; Import success
(reg-event-fx
 :admin-daily-import/import-success
 (fn [{db :db} [_ response]]
   {:db (-> db
            (assoc-in (conj business-logic-path :status) :success)
            (assoc-in (conj business-logic-path :import-result) response))
    :dispatch [:toast/success (str "Импорт успешно выполнен. "
                                   "Создано блюд: " (:dishes-created response) ", "
                                   "Добавлено позиций в меню: " (:menu-items-created response))]}))

;; Import failure
(reg-event-db
 :admin-daily-import/import-failure
 (fn [db [_ error]]
   (-> db
       (assoc-in (conj business-logic-path :status) :error)
       (assoc-in (conj business-logic-path :error) (or (:message error)
                                                        (:error error)
                                                        "Ошибка импорта")))))

;; Reset import state
(reg-event-fx
 :admin-daily-import/reset
 (fn [{db :db} _]
   {:db (assoc-in db business-logic-path
                  {:status             :idle
                   :validation-result  nil
                   :create-new-dishes? true
                   :error              nil
                   :import-result      nil})
    :fx [[:dispatch [:zf/set-value form/form-path [:file] nil]]
         [:dispatch [:zf/update-node-schema form/form-path [:file] {:errors nil}]]]}))
