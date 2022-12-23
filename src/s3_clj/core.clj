(ns s3-clj.core
  (:require [clojure.pprint :as pprint]
            [clojure.tools.logging :as log]
            [integrant.core :as ig]
            [org.httpkit.client :as http]
            [s3-clj.state :as state]))

(def default-config
  {:user "user"
   :password "password"
   :file-location "/tmp/minio"})

(defn ->ig-config [config-path]
  {:s3-clj.s3/s3 config-path})

(defn halt-s3! []
  (when @state/state
    (swap! state/state
           (fn [s]
             (ig/halt! (:system s))
             nil))))

(defmacro retry
  [cnt expr]
  (letfn [(go [cnt]
            (if (zero? cnt)
              expr
              `(try ~expr
                    (catch Exception e#
                      (retry ~(dec cnt) ~expr)))))]
    (go cnt)))

(defn init-s3
  ([] (init-s3 default-config))
  ([config]
   (let [ig-config (->ig-config config)
         config-pp (with-out-str (pprint/pprint config))]
     (log/info "starting s3 with config:" config-pp)
     (try
       (halt-s3!)
       (ig/load-namespaces ig-config)
       (reset! state/state
               {:system (ig/init ig-config)
                :config ig-config})
       (retry 30 (when (:error @(http/get (format "http://localhost:%s/" "9000")))
                   (do (Thread/sleep 100) (throw (Exception.)))))
       (catch clojure.lang.ExceptionInfo ex
         (ig/halt! (:system (ex-data ex)))
         (throw (.getCause ex)))))))

(defn with-s3-fn
  "Startup with the specified configuration; executes the function then shuts down."
  ([config f]
   (try
     (init-s3 config)
     (f)
     (finally
       (halt-s3!))))
  ([f]
   (with-s3-fn default-config f)))

(defmacro with-s3
  "Startup with the specified configuration; executes the body then shuts down."
  [config & body]
  `(with-s3-fn ~config (fn [] ~@body)))

(comment (init-s3)
         (halt-s3!))