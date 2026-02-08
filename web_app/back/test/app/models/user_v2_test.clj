(ns app.models.user-v2-test
  "Tests for user-v2 model"
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [app.models.user-v2 :as user]
            [app.db.core :as db]
            [app.server.db :as server-db]))

;; ============================================================================
;; Test fixtures
;; ============================================================================

(def test-db-config
  {:dbtype "postgresql"
   :dbname (or (System/getenv "POSTGRES_DB") "feedme_test")
   :host (or (System/getenv "POSTGRES_HOST") "localhost")
   :port (Integer/parseInt (or (System/getenv "POSTGRES_PORT") "5433"))
   :user (or (System/getenv "POSTGRES_USER") "chief")
   :password (or (System/getenv "POSTGRES_PASSWORD") "secret")})

(def test-datasource (atom nil))

(defn setup-db [f]
  (reset! test-datasource (server-db/create-datasource test-db-config))
  (f)
  ;; Cleanup is handled by database rollback or manual deletion
  )

(use-fixtures :once setup-db)

(defn cleanup-test-users []
  "Delete all test users"
  (try
    (db/execute @test-datasource
                {:delete-from :users
                 :where [:like :username "test_%"]})
    (catch Exception _)))

(use-fixtures :each (fn [f] (cleanup-test-users) (f) (cleanup-test-users)))

;; ============================================================================
;; CRUD Tests
;; ============================================================================

(deftest test-create-user
  (testing "Create user with valid data"
    (let [user-data {:username "test_user1"
                     :email "test1@example.com"
                     :password "secret123"
                     :telegram_id "123456"}
          created (user/create! @test-datasource user-data)]
      (is (some? created))
      (is (= "test_user1" (:username created)))
      (is (= "test1@example.com" (:email created)))
      (is (nil? (:password created)) "Password should not be in response")
      (is (nil? (:password_hash created)) "Password hash should be sanitized")))

  (testing "Create user with admin flags"
    (let [admin-data {:username "test_admin"
                      :email "admin@example.com"
                      :password "admin123"
                      :is_admin true
                      :is_staff true
                      :is_active true}
          created (user/create! @test-datasource admin-data)]
      (is (true? (:is_admin created)))
      (is (true? (:is_staff created)))
      (is (true? (:is_active created))))))

(deftest test-find-user
  (testing "Find user by id"
    (let [created (user/create! @test-datasource
                                {:username "test_find"
                                 :email "find@example.com"
                                 :password "pass123"})
          found (user/find-by-id @test-datasource (:id created))]
      (is (some? found))
      (is (= (:id created) (:id found)))
      (is (= "test_find" (:username found)))
      (is (nil? (:password_hash found)) "Should be sanitized")))

  (testing "Find non-existent user"
    (let [found (user/find-by-id @test-datasource 99999)]
      (is (nil? found)))))

(deftest test-find-by-username
  (testing "Find user by username"
    (user/create! @test-datasource
                  {:username "test_lookup"
                   :email "lookup@example.com"
                   :password "pass123"})
    (let [found (user/find-by-username @test-datasource "test_lookup")]
      (is (some? found))
      (is (= "test_lookup" (:username found)))
      ;; Note: find-by-username returns user WITH password_hash (for auth)
      (is (some? (:password_hash found))))))

(deftest test-update-user
  (testing "Update user email"
    (let [created (user/create! @test-datasource
                                {:username "test_update"
                                 :email "old@example.com"
                                 :password "pass123"})
          updated (user/update! @test-datasource (:id created)
                                {:email "new@example.com"})]
      (is (= "new@example.com" (:email updated)))
      (is (= "test_update" (:username updated)))
      (is (nil? (:password_hash updated)) "Should be sanitized")))

  (testing "Update user password"
    (let [created (user/create! @test-datasource
                                {:username "test_password"
                                 :email "pass@example.com"
                                 :password "oldpass123"})
          _ (user/update! @test-datasource (:id created)
                          {:password "newpass456"})
          ;; Verify new password works
          auth-result (user/authenticate @test-datasource
                                         {:username "test_password"
                                          :password "newpass456"})]
      (is (some? auth-result))
      (is (= "test_password" (:username auth-result))))))

(deftest test-delete-user
  (testing "Delete user"
    (let [created (user/create! @test-datasource
                                {:username "test_delete"
                                 :email "delete@example.com"
                                 :password "pass123"})
          deleted (user/delete! @test-datasource (:id created))
          found (user/find-by-id @test-datasource (:id created))]
      (is (some? deleted))
      (is (nil? found)))))

;; ============================================================================
;; Authentication Tests
;; ============================================================================

(deftest test-authenticate
  (testing "Authenticate with valid credentials"
    (user/create! @test-datasource
                  {:username "test_auth"
                   :email "auth@example.com"
                   :password "correct123"})
    (let [result (user/authenticate @test-datasource
                                    {:username "test_auth"
                                     :password "correct123"})]
      (is (some? result))
      (is (= "test_auth" (:username result)))
      (is (nil? (:password_hash result)) "Should be sanitized")))

  (testing "Authenticate with invalid password"
    (user/create! @test-datasource
                  {:username "test_auth2"
                   :email "auth2@example.com"
                   :password "correct123"})
    (let [result (user/authenticate @test-datasource
                                    {:username "test_auth2"
                                     :password "wrong123"})]
      (is (nil? result))))

  (testing "Authenticate with non-existent user"
    (let [result (user/authenticate @test-datasource
                                    {:username "nonexistent"
                                     :password "anypass"})]
      (is (nil? result)))))

(deftest test-change-password
  (testing "Change password with correct old password"
    (let [created (user/create! @test-datasource
                                {:username "test_change"
                                 :email "change@example.com"
                                 :password "oldpass123"})
          result (user/change-password! @test-datasource
                                        (:id created)
                                        "oldpass123"
                                        "newpass456")]
      (is (:success result))
      ;; Verify old password doesn't work
      (is (nil? (user/authenticate @test-datasource
                                   {:username "test_change"
                                    :password "oldpass123"})))
      ;; Verify new password works
      (is (some? (user/authenticate @test-datasource
                                    {:username "test_change"
                                     :password "newpass456"})))))

  (testing "Change password with incorrect old password"
    (let [created (user/create! @test-datasource
                                {:username "test_wrong"
                                 :email "wrong@example.com"
                                 :password "oldpass123"})
          result (user/change-password! @test-datasource
                                        (:id created)
                                        "wrongpass"
                                        "newpass456")]
      (is (= "Old password is incorrect" (:error result))))))

;; ============================================================================
;; User Management Tests
;; ============================================================================

(deftest test-user-activation
  (testing "Activate user"
    (let [created (user/create! @test-datasource
                                {:username "test_activate"
                                 :email "activate@example.com"
                                 :password "pass123"
                                 :is_active false})
          activated (user/activate! @test-datasource (:id created))]
      (is (true? (:is_active activated)))))

  (testing "Deactivate user"
    (let [created (user/create! @test-datasource
                                {:username "test_deactivate"
                                 :email "deactivate@example.com"
                                 :password "pass123"
                                 :is_active true})
          deactivated (user/deactivate! @test-datasource (:id created))]
      (is (false? (:is_active deactivated))))))

(deftest test-admin-management
  (testing "Make user admin"
    (let [created (user/create! @test-datasource
                                {:username "test_makeadmin"
                                 :email "makeadmin@example.com"
                                 :password "pass123"})
          admin (user/make-admin! @test-datasource (:id created))]
      (is (true? (:is_admin admin)))
      (is (true? (:is_staff admin)))))

  (testing "Revoke admin"
    (let [created (user/create! @test-datasource
                                {:username "test_revokeadmin"
                                 :email "revokeadmin@example.com"
                                 :password "pass123"
                                 :is_admin true})
          revoked (user/revoke-admin! @test-datasource (:id created))]
      (is (false? (:is_admin revoked))))))

;; ============================================================================
;; Query Tests
;; ============================================================================

(deftest test-find-all
  (testing "Find all users"
    (user/create! @test-datasource
                  {:username "test_list1"
                   :email "list1@example.com"
                   :password "pass123"})
    (user/create! @test-datasource
                  {:username "test_list2"
                   :email "list2@example.com"
                   :password "pass123"})
    (let [users (user/find-all @test-datasource)]
      (is (>= (count users) 2))
      ;; All should be sanitized
      (is (every? #(nil? (:password_hash %)) users)))))

(deftest test-pagination
  (testing "Paginate users"
    ;; Create multiple test users
    (doseq [i (range 5)]
      (user/create! @test-datasource
                    {:username (str "test_page" i)
                     :email (str "page" i "@example.com")
                     :password "pass123"}))
    (let [result (user/paginate @test-datasource {:page 1 :limit 3})]
      (is (= 3 (count (:data result))))
      (is (some? (:pagination result)))
      (is (>= (:total-items (:pagination result)) 5)))))

;; ============================================================================
;; Run tests
;; ============================================================================

(comment
  ;; Run all tests
  (clojure.test/run-tests 'app.models.user-v2-test)

  ;; Run specific test
  (test-create-user)
  (test-authenticate)
  (test-change-password))
