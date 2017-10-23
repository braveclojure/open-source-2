(ns open-source.components.project.show
  (:require [re-frame.core :as rf]))

(defn component
  []
  [:div.project-list.container
   [:h1 "Project"]
   @(rf/subscribe [:current-project])])
