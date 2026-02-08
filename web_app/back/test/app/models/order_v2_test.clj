(ns app.models.order-v2-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [app.models.order :as order]
            [app.models.user :as user]
            [app.models.dish :as dish]
            [app.models.category :as category]
            [app.db.core :as db]
            [java-time.api :as jt]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]))

;; Test database configuration
(def test-db
  {:dbtype "postgresql"
   :dbname "feedme_test"
   :host "localhost"
   :port 5433
   :user "chief"
   :password (System/getenv "POSTGRES_PASSWORD")})

(defn setup-test-db [f]
  "Setup and teardown test database for each test"
  (jdbc/with-transaction [tx test-db {:rollback-only true}]
    (binding [test-db tx]
      (f))))

(use-fixtures :each setup-test-db)

;; Helper functions

(defn create-test-user! [db]
  (user/create! db {:username "testuser"
                    :email "test@example.com"
                    :password "password123"}))

(defn create-test-category! [db]
  (category/create! db {:name "Test Category"}))

(defn create-test-dish! [db category-id]
  (dish/create! db {:name "Test Dish"
                    :category_id category-id
                    :price "15.99"
                    :kcals "500"
                    :weight "250"}))

(defn create-test-daily-menu! [db date]
  (first (sql/query db ["INSERT INTO daily_menus (date) VALUES (?) RETURNING *"
                        date])))

(defn create-test-daily-menu-item! [db menu-id dish-id]
  (first (sql/query db ["INSERT INTO daily_menu_items (daily_menu_id, dish_id, price) VALUES (?, ?, ?) RETURNING *"
                        menu-id dish-id 15.99])))

;; Tests

(deftest test-create-order
  (testing "Create order with valid dish availability"
    (let [test-user (create-test-user! test-db)
          test-category (create-test-category! test-db)
          test-dish (create-test-dish! test-db (:id test-category))
          today (jt/local-date)
          daily-menu (create-test-daily-menu! test-db today)
          _ (create-test-daily-menu-item! test-db (:id daily-menu) (:id test-dish))
          order-data {:user_id (:id test-user)
                      :dish_id (:id test-dish)
                      :quantity 2
                      :order_date today}
          created-order (order/create! test-db order-data)]
      (is (some? created-order))
      (is (= (:user_id created-order) (:id test-user)))
      (is (= (:dish_id created-order) (:id test-dish)))
      (is (= (:quantity created-order) 2))
      (is (= (:status created-order) "pending"))))

  (testing "Create order with custom status"
    (let [test-user (create-test-user! test-db)
          test-category (create-test-category! test-db)
          test-dish (create-test-dish! test-db (:id test-category))
          today (jt/local-date)
          daily-menu (create-test-daily-menu! test-db today)
          _ (create-test-daily-menu-item! test-db (:id daily-menu) (:id test-dish))
          order-data {:user_id (:id test-user)
                      :dish_id (:id test-dish)
                      :quantity 1
                      :order_date today
                      :status "completed"}
          created-order (order/create! test-db order-data)]
      (is (= (:status created-order) "completed"))))

  (testing "Create order fails when dish is not available"
    (let [test-user (create-test-user! test-db)
          test-category (create-test-category! test-db)
          test-dish (create-test-dish! test-db (:id test-category))
          today (jt/local-date)
          order-data {:user_id (:id test-user)
                      :dish_id (:id test-dish)
                      :quantity 1
                      :order_date today}]
      (is (thrown? Exception (order/create! test-db order-data))))))

(deftest test-find-order
  (testing "Find order by ID"
    (let [test-user (create-test-user! test-db)
          test-category (create-test-category! test-db)
          test-dish (create-test-dish! test-db (:id test-category))
          today (jt/local-date)
          daily-menu (create-test-daily-menu! test-db today)
          _ (create-test-daily-menu-item! test-db (:id daily-menu) (:id test-dish))
          created-order (order/create! test-db {:user_id (:id test-user)
                                                 :dish_id (:id test-dish)
                                                 :quantity 1
                                                 :order_date today})
          found-order (order/find-by-id test-db (:id created-order))]
      (is (some? found-order))
      (is (= (:id found-order) (:id created-order)))))

  (testing "Find non-existent order returns nil"
    (is (nil? (order/find-by-id test-db 99999)))))

(deftest test-update-order
  (testing "Update order status"
    (let [test-user (create-test-user! test-db)
          test-category (create-test-category! test-db)
          test-dish (create-test-dish! test-db (:id test-category))
          today (jt/local-date)
          daily-menu (create-test-daily-menu! test-db today)
          _ (create-test-daily-menu-item! test-db (:id daily-menu) (:id test-dish))
          created-order (order/create! test-db {:user_id (:id test-user)
                                                 :dish_id (:id test-dish)
                                                 :quantity 1
                                                 :order_date today})
          updated-order (order/update! test-db (:id created-order) {:status "completed"})]
      (is (= (:status updated-order) "completed"))
      (is (= (:id updated-order) (:id created-order)))))

  (testing "Update order quantity"
    (let [test-user (create-test-user! test-db)
          test-category (create-test-category! test-db)
          test-dish (create-test-dish! test-db (:id test-category))
          today (jt/local-date)
          daily-menu (create-test-daily-menu! test-db today)
          _ (create-test-daily-menu-item! test-db (:id daily-menu) (:id test-dish))
          created-order (order/create! test-db {:user_id (:id test-user)
                                                 :dish_id (:id test-dish)
                                                 :quantity 1
                                                 :order_date today})
          updated-order (order/update! test-db (:id created-order) {:quantity 3})]
      (is (= (:quantity updated-order) 3)))))

(deftest test-delete-order
  (testing "Delete existing order"
    (let [test-user (create-test-user! test-db)
          test-category (create-test-category! test-db)
          test-dish (create-test-dish! test-db (:id test-category))
          today (jt/local-date)
          daily-menu (create-test-daily-menu! test-db today)
          _ (create-test-daily-menu-item! test-db (:id daily-menu) (:id test-dish))
          created-order (order/create! test-db {:user_id (:id test-user)
                                                 :dish_id (:id test-dish)
                                                 :quantity 1
                                                 :order_date today})
          deleted-order (order/delete! test-db (:id created-order))]
      (is (some? deleted-order))
      (is (nil? (order/find-by-id test-db (:id created-order)))))))

(deftest test-find-by-user
  (testing "Find all orders for a user"
    (let [test-user (create-test-user! test-db)
          test-category (create-test-category! test-db)
          test-dish (create-test-dish! test-db (:id test-category))
          today (jt/local-date)
          daily-menu (create-test-daily-menu! test-db today)
          _ (create-test-daily-menu-item! test-db (:id daily-menu) (:id test-dish))
          order1 (order/create! test-db {:user_id (:id test-user)
                                         :dish_id (:id test-dish)
                                         :quantity 1
                                         :order_date today})
          order2 (order/create! test-db {:user_id (:id test-user)
                                         :dish_id (:id test-dish)
                                         :quantity 2
                                         :order_date today})
          user-orders (order/find-by-user test-db (:id test-user))]
      (is (= (count user-orders) 2))
      (is (some #(= (:id %) (:id order1)) user-orders))
      (is (some #(= (:id %) (:id order2)) user-orders)))))

(deftest test-find-by-date
  (testing "Find all orders for a specific date"
    (let [test-user (create-test-user! test-db)
          test-category (create-test-category! test-db)
          test-dish (create-test-dish! test-db (:id test-category))
          today (jt/local-date)
          tomorrow (jt/plus today (jt/days 1))
          daily-menu1 (create-test-daily-menu! test-db today)
          daily-menu2 (create-test-daily-menu! test-db tomorrow)
          _ (create-test-daily-menu-item! test-db (:id daily-menu1) (:id test-dish))
          _ (create-test-daily-menu-item! test-db (:id daily-menu2) (:id test-dish))
          order1 (order/create! test-db {:user_id (:id test-user)
                                         :dish_id (:id test-dish)
                                         :quantity 1
                                         :order_date today})
          order2 (order/create! test-db {:user_id (:id test-user)
                                         :dish_id (:id test-dish)
                                         :quantity 1
                                         :order_date tomorrow})
          today-orders (order/find-by-date test-db today)]
      (is (= (count today-orders) 1))
      (is (= (:id (first today-orders)) (:id order1))))))

(deftest test-find-by-status
  (testing "Find orders by status"
    (let [test-user (create-test-user! test-db)
          test-category (create-test-category! test-db)
          test-dish (create-test-dish! test-db (:id test-category))
          today (jt/local-date)
          daily-menu (create-test-daily-menu! test-db today)
          _ (create-test-daily-menu-item! test-db (:id daily-menu) (:id test-dish))
          pending-order (order/create! test-db {:user_id (:id test-user)
                                                :dish_id (:id test-dish)
                                                :quantity 1
                                                :order_date today
                                                :status "pending"})
          completed-order (order/create! test-db {:user_id (:id test-user)
                                                  :dish_id (:id test-dish)
                                                  :quantity 1
                                                  :order_date today
                                                  :status "completed"})
          pending-orders (order/find-pending test-db)
          completed-orders (order/find-completed test-db)]
      (is (>= (count pending-orders) 1))
      (is (>= (count completed-orders) 1))
      (is (some #(= (:id %) (:id pending-order)) pending-orders))
      (is (some #(= (:id %) (:id completed-order)) completed-orders)))))

(deftest test-order-management
  (testing "Complete order"
    (let [test-user (create-test-user! test-db)
          test-category (create-test-category! test-db)
          test-dish (create-test-dish! test-db (:id test-category))
          today (jt/local-date)
          daily-menu (create-test-daily-menu! test-db today)
          _ (create-test-daily-menu-item! test-db (:id daily-menu) (:id test-dish))
          created-order (order/create! test-db {:user_id (:id test-user)
                                                :dish_id (:id test-dish)
                                                :quantity 1
                                                :order_date today})
          completed-order (order/complete-order! test-db (:id created-order))]
      (is (= (:status completed-order) "completed"))))

  (testing "Cancel order"
    (let [test-user (create-test-user! test-db)
          test-category (create-test-category! test-db)
          test-dish (create-test-dish! test-db (:id test-category))
          today (jt/local-date)
          daily-menu (create-test-daily-menu! test-db today)
          _ (create-test-daily-menu-item! test-db (:id daily-menu) (:id test-dish))
          created-order (order/create! test-db {:user_id (:id test-user)
                                                :dish_id (:id test-dish)
                                                :quantity 1
                                                :order_date today})
          cancelled-order (order/cancel-order! test-db (:id created-order))]
      (is (= (:status cancelled-order) "cancelled")))))

(deftest test-order-with-details
  (testing "Get order with user and dish details"
    (let [test-user (create-test-user! test-db)
          test-category (create-test-category! test-db)
          test-dish (create-test-dish! test-db (:id test-category))
          today (jt/local-date)
          daily-menu (create-test-daily-menu! test-db today)
          _ (create-test-daily-menu-item! test-db (:id daily-menu) (:id test-dish))
          created-order (order/create! test-db {:user_id (:id test-user)
                                                :dish_id (:id test-dish)
                                                :quantity 1
                                                :order_date today})
          order-details (order/get-order-with-details test-db (:id created-order))]
      (is (some? order-details))
      (is (= (:user_username order-details) (:username test-user)))
      (is (= (:dish_name order-details) (:name test-dish)))
      (is (= (:dish_price order-details) (:price test-dish))))))

(deftest test-user-order-summary
  (testing "Get user order summary"
    (let [test-user (create-test-user! test-db)
          test-category (create-test-category! test-db)
          test-dish (create-test-dish! test-db (:id test-category))
          today (jt/local-date)
          daily-menu (create-test-daily-menu! test-db today)
          _ (create-test-daily-menu-item! test-db (:id daily-menu) (:id test-dish))
          _ (order/create! test-db {:user_id (:id test-user)
                                    :dish_id (:id test-dish)
                                    :quantity 2
                                    :order_date today})
          _ (order/create! test-db {:user_id (:id test-user)
                                    :dish_id (:id test-dish)
                                    :quantity 1
                                    :order_date today})
          summary (order/get-user-order-summary test-db (:id test-user))]
      (is (= (:total_orders summary) 2))
      (is (> (:total_spent summary) 0)))))
