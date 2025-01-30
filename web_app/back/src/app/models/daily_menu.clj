(ns app.models.daily-menu
  (:require [app.helpers :as h]
            [app.models.crud :as crud :refer [CRUD]]
            [app.server.db :refer [execute-query]]
            [app.macro :refer [persist-scope]]))

(def DailyMenuSchema
  [:map
   [:date [:time/local-date]]
   [:is_published {:optional true} [:boolean]]])

(defrecord DailyMenuModel [table-name schema datasource-or-tx]
  CRUD
  (create! [_ data]
    (persist-scope)
    (if-let [errors (h/validate-data schema (or data {}))]
      (throw (ex-info "Validation failed" {:errors errors}))
      (let [query {:insert-into table-name
                   :values      [data]
                   :returning   [:*]}]
        (first (execute-query datasource-or-tx query)))))

  (read [_ id]
    (let [query {:select    [:dm.* [:array_agg :dmi.*] :menu_items]
                 :from      [[table-name :dm]]
                 :left-join [[:daily_menu_items :dmi] [:= :dm.id :dmi.daily_menu_id]]
                 :where     [:= :dm.id [:cast id :integer]]
                 :group-by  [:dm.id]}]
      (first (execute-query datasource-or-tx query))))

  (update! [_ id data]
    (if-let [errors (h/validate-data schema data)]
      (throw (ex-info "Validation failed" {:errors errors}))
      (let [query {:update    table-name
                   :set       data
                   :where     [:= :id [:cast id :integer]]
                   :returning [:*]}]
        (first (execute-query datasource-or-tx query)))))

  (delete! [_ id]
    (let [query {:delete-from table-name
                 :where       [:= :id [:cast id :integer]]
                 :returning   [:*]}]
      (first (execute-query datasource-or-tx query))))
  
  (list-all [_ {:keys [date] :as _query-params}]
    (let [query (cond-> {:select   [:dm.*
                                    [[:jsonb_agg [:to_jsonb :dmi.*]] :menu_items]]
                         :from     [[table-name :dm]]
                         :join     [[:daily_menu_items :dmi]
                                    [:= :dm.id :dmi.daily_menu_id]]
                         :where    (cond-> [:and true]
                                     date
                                     (conj [:= :date [:cast date :date]]))
                         :group-by [:dm.id]})]
      (app.macro/persist-scope)
      (->> (execute-query datasource-or-tx query)
           (mapv (fn [row]
                   {:menu (dissoc row :menu_items)
                    :menu-items (:menu_items row)}))))))

(defn model [datasource]
  (->DailyMenuModel :daily_menus DailyMenuSchema datasource))

(defn publish-menu [datasource menu-id]
  (let [model (model datasource)
        data {:is_published true
              :published_at (java.time.LocalDateTime/now)}]
    (crud/update! model menu-id data)))

(defn unpublish-menu [datasource menu-id]
  (let [model (model datasource)
        data {:is_published false
              :published_at nil}]
    (crud/update! model menu-id data)))
