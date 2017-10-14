(ns dev
  (:require [clojure.java.io :as io]
            [duct.core :as duct]))

(defn read-config []
  (duct/read-config (io/resource "dev.edn")))

(def prep (comp duct/prep read-config))
