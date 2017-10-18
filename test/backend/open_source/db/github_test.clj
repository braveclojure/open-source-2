(ns open-source.db.github-test
  (:require [open-source.db.github :as gh]
            [clojure.test :refer [is deftest]]))

(deftest filter-deleted-file-paths
  (is (= (gh/filter-deleted-file-paths {"projects/alda.edn" {}} [])
         #{"projects/alda.edn"}))

  (is (= (gh/filter-deleted-file-paths {"projects/alda.edn" {}
                                        "projects/balda.edn" {}}
                                       [{:path "projects/balda.edn"}])
         #{"projects/alda.edn"})))


(deftest updated-files
  (is (empty? (gh/filter-updated-files {"projects/alda.edn" {:sha "x"}}
                                       [{:path "projects/alda.edn"
                                         :sha "x"}])))

  (is (= (gh/filter-updated-files {"projects/alda.edn" {:sha "x"}}
                                  [{:path "projects/alda.edn"
                                    :sha "y"}])
         [{:path "projects/alda.edn"
           :sha "y"}])))


