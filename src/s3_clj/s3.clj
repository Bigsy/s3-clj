(ns s3-clj.s3
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as sh]
            [integrant.core :as ig]
            [org.httpkit.client :as http]
            [clojure.tools.logging :as log]
            [babashka.process :refer [shell sh process check]])
  (:import [java.io File]
           [java.nio.file Files Paths LinkOption Path]
           [java.nio.file.attribute FileAttribute]))

(def ^:private download-m1 "https://dl.min.io/server/minio/release/darwin-arm64/minio")
(def ^:private download-macintel "https://dl.min.io/server/minio/release/darwin-amd64/minio")
(def ^:private download-linuxintel "https://dl.min.io/server/minio/release/linux-amd64/minio")


(def s3-directory (str (System/getProperty "user.home") File/separator ".clj-s3-local"))

(def ^:private host {:name    (clojure.string/lower-case (System/getProperty "os.name"))
                     :version (System/getProperty "os.version")
                     :arch    (System/getProperty "os.arch")})

(defn- ->path
  "Create a path from the given strings."
  [str & strs]
  {:pre [(string? str) (every? string? strs)]}
  (Paths/get str (into-array String strs)))

(defn- path?
  "Is the given argument a path?"
  [x]
  (instance? Path x))

(defn- exists?
  "Does the given path exist?"
  [path]
  {:pre [(path? path)]}
  (Files/exists path (into-array LinkOption [])))

(defn- ensure-s3-directory
  "Make sure the directory that s3 Local will be downloaded to
  exists."
  []
  (let [path (->path s3-directory)]
    (when-not (exists? path)
      (-> (Files/createDirectory path (make-array FileAttribute 0))
          (.toString)))))



(defn start-s3
  "Start s3 Local with the desired options."
  [{:keys [user password file-location] :as config}]
  (let [s3 (:proc (process {:err :inherit
                            :extra-env {"MINIO_ROOT_USER"     user
                                        "MINIO_ROOT_PASSWORD" password}}
                           "bash -c" (format "%s/%s server %s" s3-directory "s3" file-location)))]
    (log/info "Started s3 Local")
    s3))

(defn- download-s3
  "Download s3."
  [url]
  (log/info "Downloading s3 Local - this can take upto a few mins on the first run - killing can leave a broken binary here: "
             s3-directory)
  (ensure-s3-directory)
  (io/copy (io/input-stream (:body @(http/get url {:as :stream}))) (io/as-file (str s3-directory "/" "s3")))
  (.setExecutable (io/file s3-directory "s3") true))

(defn ensure-installed
  "Download and s3 Local if it hasn't been already."
  []
  (when-not (exists? (->path s3-directory "s3"))
    (if (clojure.string/starts-with? (:name host) "mac")
      (if (= "aarch64" (:arch host))
        (download-s3 download-m1)
        (download-s3 download-macintel))
      (download-s3 download-linuxintel))))


(defn handle-shutdown
  "Kill the s3 Local process on JVM shutdown."
  [s3-process]
  (doto s3-process (.destroy) (.waitFor))
  (log/info (str "Exited" {:exit-value (.exitValue s3-process)})))

(defn create-s3-db-logger
  [log]
  (fn [& message]
    (apply log "s3local:" message)))

(defn halt! [s3]
  (when s3
    (handle-shutdown s3)))

(defmethod ig/init-key ::s3 [_ config]
  (ensure-installed)
  (start-s3 config))

(defmethod ig/halt-key! ::s3 [_ s3]
  (halt! s3))