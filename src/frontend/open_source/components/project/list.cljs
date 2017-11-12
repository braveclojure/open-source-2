(ns open-source.components.project.list
  (:require [re-frame.core :as rf]
            [sweet-tooth.frontend.form.components :as stfc]
            [open-source.components.ui :as ui]
            [open-source.utils :as u]
            [open-source.flows.project :as project-flow]))

(defn filter-tag
  [tags tag]
  [:span.tag-container
   [:span.tag {:class (if (get tags tag) "active")
               :data-prevent-nav true
               :on-click #(do (.stopPropagation %)
                              (.preventDefault %)
                              (rf/dispatch [::project-flow/toggle-tag tag]))} tag]])

(defn project
  [{:keys [project/stats project/beginner-friendly] :as p} selected-tags]
  [:tr.project
   {:class (if beginner-friendly "beginner-friendly")}
   [:td
    [:a.project-main {:href (:slug p)}
     [:span.name (:project/name p)]
     [:span.tagline (:project/tagline p)]]
    (if (:project/beginner-friendly p)
      [:div.beginner-friendly [:i.fa.fa-check] " beginner friendly"])
    (if-let [t (:record/split-tags p)]
      [:div.tags
       (for [tag t]
         ^{:key (gensym)} [filter-tag selected-tags tag])])]
   [:td.home-page [:a {:href (:project/home-page-url p)} [:i.fa.fa-globe]]]
   [:td.repo [:a {:href (:project/repo-url p)} [:i.fa.fa-code-fork]]]
   [:td.stargazers
    (when stats
      [:span.stargazers [:i.fa.fa-star] (:stargazers-count stats)])]
   [:td.stargazers
    (when stats
      [:span.last-pushed
       {:title "days since last push"}
       [:i.fa.fa-clock-o]
       (:days-since-push stats)])]])

(defn project-list
  []
  [:div.main.listings.public
   [:table.projects
    [:thead
     [:tr
      [:th "project"]
      [:th] [:th]
      [:th [:i.fa.fa-star]]
      [:th [:i.fa.fa-clock-o]]]]
    [:tbody
     (let [selected-tags (-> (stfc/form project-flow/filter-form-path)
                             :form-data
                             deref
                             :selected-tags)]
       (->> @(rf/subscribe [::project-flow/filtered-projects])
            (sort-by :slug)
            (map (fn [p]
                   ^{:key (str "os-project-" (:slug p))}
                   [project p selected-tags]))))]]])

(defn filter-tags
  [tag-source]
  (let [selected-tags (:selected-tags @tag-source)
        tags          @(rf/subscribe [::project-flow/tags])]
    [:div.section.tags
     [:div
      (for [tag tags]
        ^{:key (gensym)} [filter-tag selected-tags tag])]]))

(defn sidebar
  []
  (let [{:keys [input form-data]} (stfc/form project-flow/filter-form-path)] 
    [:div.secondary.listings
     [:div.section.search
      [input :search :query
       :no-label true
       :placeholder "Search: `music`, `database` ..."]]
     [:div.section.beginner-toggle
      [input :checkbox :project/beginner-friendly :label "Beginner friendly?"]]
     [filter-tags form-data]
     [:div.section
      [:div.details
       [:h3 "Learn Clojure"]
       [:div.book-cover [:img {:src "/images/book-cover.png"}]]
       [:p [:a {:href "http://braveclojure.com"} [:em "Clojure for the Brave and True"]]
        " is a fun, in-depth introduction to Clojure. Read
                   it "
        [:a {:href "http://braveclojure.com"} "free online"]
        " or "
        [:a {:href "http://amzn.to/1mz8qNR"} "buy the print
                   or ebook on Amazon"]
        "!"]]]]))

(defn component
  []
  [:div.project-list.container
   [:div.intro
    [:div.main
     [:p "These projects are under active development and welcome new contributors."]]
    [:div.secondary
     [:a {:href "/projects/new"} "Post Your Project"]]]
   [project-list]
   [sidebar]])
