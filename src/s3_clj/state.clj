(ns s3-clj.state
  (:require [clojure.tools.namespace.repl :as repl]))

(repl/disable-reload!)

(def state (atom nil))

