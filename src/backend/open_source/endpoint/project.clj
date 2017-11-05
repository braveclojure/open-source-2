(ns open-source.endpoint.project
  (:require [integrant.core :as ig]
            [open-source.endpoint.common :as lc]
            [open-source.db.github :as osgh]
            [sweet-tooth.endpoint.utils :as eu]
            [compojure.core :refer :all]))

(defn list-projects
  [db]
  (fn [_]
    (->> db
         osgh/project-list
         (eu/ent-type :project)
         lc/format-ent)))

(defn write-project
  [db]
  (fn [ctx] (osgh/write-project! db (eu/params ctx))))

(defn decisions
  [{:keys [db]}]
  {:list   {:handle-ok (list-projects db)}
   :update {:put! (write-project db)
            :handle-ok (list-projects db)}
   :create {:post! (write-project db)
            :handle-created (list-projects db)}})

(def endpoint (lc/endpoint "/api/project" decisions))

(defmethod ig/init-key :open-source.endpoint/project [_ options]
  (endpoint options))
