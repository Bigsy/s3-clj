(ns s3-clj.custom-test
  (:require [clojure.test :refer :all]
            [cognitect.aws.client.api :as aws]
            [cognitect.aws.credentials :as credentials]
            [s3-clj.core :as sut]
            [s3-clj.s3 :as ms3]))

(defn delete-recursively [fname]
  (doseq [f (reverse (file-seq (clojure.java.io/file fname)))]
    (clojure.java.io/delete-file f)))

(defn around-all
  [f]
  (sut/with-s3-fn {:user "wibble"
                   :password "wibble1234"
                   :file-location ms3/s3-directory} f))

(use-fixtures :once around-all)


(def s3 (aws/client {:api                  :s3
                     :region "us-east-1"
                     :credentials-provider (credentials/basic-credentials-provider
                                             {:access-key-id     "wibble"
                                              :secret-access-key "wibble1234"})
                     :endpoint-override {:protocol :http
                                         :hostname "localhost"
                                         :port     9000}}))

(deftest can-wrap-around
  (testing "using custom db file"
    (aws/invoke s3 {:op :CreateBucket :request {:Bucket "wibble"}})
    (is (= "wibble" (get-in (aws/invoke s3 {:op :ListBuckets}) [:Buckets 0 :Name])))
    (aws/invoke s3 {:op :DeleteBucket :request {:Bucket "wibble"}})
    (is (empty? (get-in (aws/invoke s3 {:op :ListBuckets}) [:Buckets 0])))
    (is (.exists (clojure.java.io/file (str ms3/s3-directory "/.minio.sys"))))
    (delete-recursively (clojure.java.io/file (str ms3/s3-directory "/.minio.sys")))))


(comment (aws/ops s3))