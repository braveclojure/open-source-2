(ns open-source.components.project.list
  (:require [re-frame.core :as rf]))

(defn project-list
  []
  (into [:div.projects (map (fn [p] [:div.project (:project/name p)])
                            @(rf/subscribe [:projects]))]))

(defn component
  []
  [:div.project-list.container
   [:h1 "Projects"]
   [project-list]])
