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

(defn decisions
  [{:keys [db]}]
  {:list   {:handle-ok (list-projects db)}
   :update {:put! (fn [ctx] (osgh/write-project! db (eu/params ctx)))
            :handle-ok (list-projects db)}
   :create {:post! (fn [ctx] (osgh/write-project! db (eu/params ctx)))
            :handle-created (list-projects db)}})

(def endpoint (lc/endpoint "/api/project" decisions))

(defmethod ig/init-key :open-source.endpoint/project [_ options]
  (endpoint options))
