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

(deftest github-project-params
  (is (= (gh/github-project-params {:project/name "alda"
                                    :sha "x"})
         {:message "updating alda via web",
          :content "ezpwcm9qZWN0L25hbWUgImFsZGEiLAogOnByb2plY3QvcmVwby11cmwgbmlsLAogOnByb2plY3QvaG9tZS1wYWdlLXVybCBuaWx9Cg=="
          :sha "x"}))
  (is (= (gh/github-project-params {:project/name "alda"})
         {:message "updating alda via web",
          :content "ezpwcm9qZWN0L25hbWUgImFsZGEiLAogOnByb2plY3QvcmVwby11cmwgbmlsLAogOnByb2plY3QvaG9tZS1wYWdlLXVybCBuaWx9Cg=="})))

(deftest project-file-body
  (is (= (gh/project-file-body {:project/name "project"})
         "{:project/name \"project\",
 :project/repo-url nil,
 :project/home-page-url nil}
")))
