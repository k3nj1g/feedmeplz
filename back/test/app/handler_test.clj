(ns app.handler-test
  (:require [clojure.test :refer [deftest is testing]]
            
            [ring.mock.request :as mock]
            
            [app.handler :as sut]))

(deftest test-app
  (testing "main route"
    (let [response (sut/main (mock/request :get "/"))]
      (is (= 200 (:status response))))))
