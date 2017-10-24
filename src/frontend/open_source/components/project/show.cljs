(ns open-source.components.project.show
  (:require [re-frame.core :as rf]
            [open-source.components.ui :as ui]))

(defn component
  []
  (fn []
    (let [project @(rf/subscribe [:current-project])]
      [:div.container
       [:div.listings
        [:div.view-all [:a {:href "/"} "← view all open source projects"]]
        [:div.os-project.detail.clearfix
         [:div.meta
          [:div.title [ui/attr project :project/name]]
          (if-let [t (:project/tagline project)]
            [:div.tagline t])]
         [:div.secondary
          [:div.section
           [:a {:href (str "/" (:slug project) "/edit")}
            [:i.fa.fa-pencil]
            " Edit"]]
          (let [repo-url (:project/repo-url project)
                bil      (:project/beginner-issues-label project)]
            [:div.links
             (if repo-url
               [:div.repo-url [ui/ext-link repo-url "repo"]])
             (if (and repo-url (not-empty bil) (re-find #"github" repo-url))
               [:div.beginner-issues [ui/ext-link (str repo-url "/labels/" bil) "beginner-friendly tasks"]])
             (if-let [hp (:project/home-page-url project)]
               [:div.home-page-url [ui/ext-link hp "home page"]])])
          (if-let [t (:record/tags project)]
            [:div.tags [ui/tags t]])]
         [:div.description
          [:div.project-description (ui/markdown (:project/description project))]]]]])))
