(ns open-source.components.project.create
  (:require [re-frame.core :as rf]
            [open-source.components.ui :as ui]
            [open-source.components.project.form :as pf]))

(defn component
  []
  [:div.container
   [:div.edit-listing
    [:div.title [:h1 "Post an Open Source Project"]]
    [:div.wizard
     [:div.spiff.inset
      [pf/form nil]]]]])
