(defproject clj-mqtt-component "0.1.0-SNAPSHOT"
  :description "Mqtt component"
  :url "Mqtt component"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [clojurewerkz/machine_head "1.0.0"]
                 [org.clojure/data.json "0.2.6"]
                 [com.stuartsierra/component "0.3.2"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
