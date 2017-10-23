(ns open-source.endpoint.static
  (:require [compojure.core :refer :all]
            [ring.util.response :as resp]
            [sweet-tooth.endpoint.utils :as eu]
            [integrant.core :as ig]))


(defmethod ig/init-key :open-source.endpoint/static [_ options]
  (routes (GET "/main.js" [] (resp/resource-response "main.js"))
          ;; load the single page app
          (GET "/" [] (eu/html-resource "index.html"))
          (GET "/project/*" [] (eu/html-resource "index.html"))))
