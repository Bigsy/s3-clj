# s3-clj

Embedded fake s3 for clojure based on minio, just packaged to make it easy to use for mocking in clojure land

s3-clj.sqs-test.clj provides a good example of how to test and integration with sqs (using elasticmq)

## Usage

[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.bigsy/s3-clj.svg)](https://clojars.org/org.clojars.bigsy/s3-clj)
### Development:

```clojure
(require 's3-clj.core)

;; Start a local s3 with default port:
(init-s3)

;; another call will halt the previous system:
(init-s3)

;; When you're done:
(halt-s3!)
```

### Testing:

**NOTE**: these will halt running s3 instances

```clojure
(require 'clojure.test)

(use-fixtures :once with-s3-fn)

(defn around-all
  [f]
  (with-s3-fn {optional config map}
                    f))

(use-fixtures :once around-all)


; You can also wrap ad-hoc code in init/halt:
(with-s3 {optional config map}
  do-something) 


;default config map options that can be changed and passed in
{:user "user"
 :password "password"
 :file-location "/tmp/minio"}
  ```
