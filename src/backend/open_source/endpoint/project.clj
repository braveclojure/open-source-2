(ns open-source.endpoint.project
  (:require [integrant.core :as ig]
            [open-source.endpoint.common :as lc]
            [open-source.db.github :as osgh]
            [sweet-tooth.endpoint.utils :as eu]
            [compojure.core :refer :all]))

(defn decisions
  [options]
  {:list {:handle-ok (fn [ctx]
                       (let [x (->> @osgh/projects
                                    (map #(osgh/merge-github-data % {}))
                                    (eu/ent-type :project)
                                    lc/format-ent)]
                         x))}})

(def endpoint (lc/endpoint "/api/projects" decisions))

(defmethod ig/init-key :open-source.endpoint/project [_ options]
  (endpoint options))
