(ns open-source.components.project.edit
  (:require [re-frame.core :as rf]
            [open-source.components.ui :as ui]
            [open-source.components.project.form :as pf]))

(defn component
  []
  [:div.container
   [:div.edit-listing
    [:div.title [:h1 "Edit Project"]]
    (let [project @(rf/subscribe [:current-project])]
      [:div.wizard
       [:div.spiff.inset
        (when project [pf/form (:db/id project)])]])]])
