(ns clj-mqtt-component.core
  (:require [com.stuartsierra.component :as comp]
            [clojurewerkz.machine-head.client :as mh]
            [clojure.data.json :as json]))

(defprotocol ExtendedTopic
  (publish [_ topic payload])
  (subscribe [_ topic call-back-fn])
  (publish-and-wait-response [_ topic payload])
  (subscribe-and-answer [_ topic call-back-fn]))

(defrecord Mqtt [conn promises opts])

(extend-type Mqtt
  comp/Lifecycle
  (start [{:keys [opts] :as this}]
    (let [conn (mh/connect (:url opts) {:client-id (:client-id opts)})
          promises (atom {})
          init-cmp (assoc this
                          :conn conn
                          :promises promises)]

      (subscribe init-cmp "responses" (fn [[response-id response]]
                                        ;; when it's a response for this component
                                        (when-let [p (get @promises response-id)]
                                          (deliver p response)
                                          (swap! promises dissoc response-id))))
      
      init-cmp))
  
  (stop [this]
    (mh/disconnect (:conn this))
    (assoc this :conn nil))

  ExtendedTopic
  (publish [{:keys [conn]} topic payload]
    (mh/publish conn topic (json/write-str payload)))
  
  (subscribe [{:keys [conn] :as this} topic call-back-fn]
    (mh/subscribe conn
                  {topic 0}
                  (fn [^String topic _ ^bytes payload]
                    (try
                      (call-back-fn (json/read-str (String. payload "UTF-8")
                                                   :key-fn keyword))
                      (catch Exception e (.printStackTrace e))))))
  
  (publish-and-wait-response [{:keys [promises] :as this} topic payload]
    (let [request-id (.toString (java.util.UUID/randomUUID))
          p (promise)]
      (publish this topic {:request-id request-id :payload payload})
      (swap! promises assoc request-id p)
      (deref p 10000 nil)))

  (subscribe-and-answer [this topic call-back-fn]
    (subscribe this topic (fn [{:keys [request-id payload] :as a}]
                            (publish this "responses"
                                     [request-id (call-back-fn payload)])))))

(defn make-mqtt [opts]
  (map->Mqtt {:opts opts}))
