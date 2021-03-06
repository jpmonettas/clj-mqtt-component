(ns clj-mqtt-component.core-test
  (:require [clojure.test :refer :all]
            [clj-mqtt-component.core :refer :all]
            [com.stuartsierra.component :as comp]))


(deftest publish-subscribe
  (let [c1 (comp/start (make-mqtt {:url "tcp://127.0.0.1:1883"
                                   :client-id "test1"}))
        c2 (comp/start (make-mqtt {:url "tcp://127.0.0.1:1883"
                                   :client-id "test2"}))
        p1 (promise)]
    (subscribe c1 "t1" #(deliver p1 %))
    (publish c2 "t1" {:name "John"})
    (is (= (:name @p1) "John"))
    (comp/stop c1)
    (comp/stop c2)))

(deftest publish-subscribe-response
  (let [c1 (comp/start (make-mqtt {:url "tcp://127.0.0.1:1883"
                                   :client-id "test3"}))
        c2 (comp/start (make-mqtt {:url "tcp://127.0.0.1:1883"
                                   :client-id "test4"}))
        _ (subscribe-and-answer c2 "t1"  #(repeat (:gimmy-numbers %) 1))
        f1 (future (publish-and-wait-response c1 "t1" {:gimmy-numbers 3}))
        v1 (deref f1 2000 [])]
    
    (is (= (count v1) 3))
    (is (.isConnected (:conn c1)))
    (is (.isConnected (:conn c2)))
    (comp/stop c1)
    (comp/stop c2)))


