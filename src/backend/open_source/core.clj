(ns open-source.core
  (:gen-class)
  (:require [clojure.java.io :as io]
            [clojure.stacktrace :as stacktrace]
            [duct.core :as duct]
            [integrant.core :as ig]))

(defmacro final
  [& body]
  `(do (try (do ~@body)
            (catch Exception exc#
              (do (println "ERROR: " (.getMessage exc#))
                  (stacktrace/print-stack-trace exc#)
                  (System/exit 1))))
       (System/exit 0)))

(defn prep
  []
  (duct/prep (duct/read-config (io/resource "open_source/config.edn"))))

(defn -main
  [cmd & args]
  (case cmd "server" (ig/init (prep) [:duct/daemon])))
