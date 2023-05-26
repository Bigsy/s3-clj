(ns s3-clj.default-test
  (:require [clojure.test :refer :all]
            [cognitect.aws.client.api :as aws]
            [cognitect.aws.credentials :as credentials]
            [s3-clj.core :as sut]))

(defn around-all
  [f]
  (sut/with-s3-fn f))

(use-fixtures :once around-all)


(def s3 (aws/client {:api                  :s3
                     :region "us-east-1"
                     :credentials-provider (credentials/basic-credentials-provider
                                             {:access-key-id     "user"
                                              :secret-access-key "password"})
                     :endpoint-override {:protocol :http
                                         :hostname "localhost"
                                         :port     9000}}))

(deftest can-wrap-around
  (testing "using custom db file"
    (aws/invoke s3 {:op :CreateBucket :request {:Bucket "wibble"}})
    (is (= "wibble" (get-in (aws/invoke s3 {:op :ListBuckets}) [:Buckets 0 :Name])))
    (aws/invoke s3 {:op :DeleteBucket :request {:Bucket "wibble"}})
    (is (empty? (get-in (aws/invoke s3 {:op :ListBuckets}) [:Buckets 0])))))

