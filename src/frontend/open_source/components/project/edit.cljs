(ns open-source.components.project.edit
  (:require [re-frame.core :as rf]
            [open-source.components.ui :as ui]
            [open-source.components.project.form :as pf]
            [open-source.flows.project :as project-flow]))

(defn component
  []
  (let [project @(rf/subscribe [::project-flow/current-project])]
    [:div.container
     [:div.edit-listing
      [:div.view-all
       [:a {:href (str "/" (:slug project))} (str "← " (:project/name project))]]
      [:div.title [:h1 "Edit Project"]]
      [:div.wizard
       [:div.spiff.inset
        (when project [pf/form (:db/id project)])]]]]))
