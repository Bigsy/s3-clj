(defproject org.clojars.bigsy/s3-clj "0.1.0"
  :description "s3 fake for clojure"
  :url "https://github.com/Bigsy/s3-clj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [integrant "0.8.0"]
                 [org.clojure/tools.logging "1.1.0"]
                 [org.clojure/tools.namespace "1.1.0"]
                 [org.slf4j/slf4j-jdk14 "1.7.30"]
                 [http-kit "2.5.3"]
                 [babashka/process "0.4.13"]]

  :profiles {:dev {:dependencies [[com.cognitect.aws/api "0.8.630"]
                                  [com.cognitect.aws/endpoints "1.1.12.358"]
                                  [com.cognitect.aws/s3 "825.2.1250.0"]
                                  [cheshire "5.11.0"]]}})

