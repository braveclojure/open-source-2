(ns open-source.endpoint.common
  (:require [compojure.core :refer [routes]]
            [medley.core :as medley]
            [sweet-tooth.frontend.core.utils :as u]
            [sweet-tooth.endpoint.utils :as c])
  (:refer-clojure :exclude [format]))

(defn format-ent
  [e]
  {:entity (c/format-ent e :db/id)})

(defn endpoint
  [route decisions]
  (fn [component]
    (routes (c/resource-route route decisions component))))
