(ns open-source.components.project.list
  (:require [re-frame.core :as rf]
            [sweet-tooth.frontend.form.components :as stfc]
            [open-source.components.ui :as ui]
            [open-source.utils :as u]))

(defn project-list
  []
  [:table.projects
   [:tbody
    (->> @(rf/subscribe [:projects])
         vals
         (sort-by :slug)
         (map (fn [{:keys [project/stats] :as p}]
                ^{:key (str "os-project-" (:slug p))}
                [:tr.project
                 [:td
                  [:a.project-main {:href (:slug p)}
                   [:span.name (:project/name p)]
                   [:span.tagline (:project/tagline p)]]]
                 [:td.home-page [:a {:href (:project/home-page-url p)} [:i.fa.fa-globe]]]
                 [:td.repo [:a {:href (:project/repo-url p)} [:i.fa.fa-code-fork]]]
                 [:td.stargazers
                  (when stats
                    [:span.stargazers [:i.fa.fa-star] (:stargazers-count stats)])]
                 [:td.stargazers
                  (when stats
                    [:span.last-pushed [:i.fa.fa-clock-o]
                     (:days-since-push stats)])]])))]])

(defn component
  []
  [:div.project-list.container
   [:div.intro
    [:div.main
     [:p "These projects are under active development and welcome new contributors."]]
    [:div.secondary
     [:a {:href "/projects/new"} "Post Your Project"]]]
   [:div.main.listings.public
    [project-list]]])

(defn filter-tag
  [tags tag]
  [:span.tag-container
   [:span.tag {:class (if (get tags tag) "active")
               :data-prevent-nav true
               :on-click #(do (.stopPropagation %)
                              (.preventDefault %)
                              (rf/dispatch [:toggle-tag tag]))} tag]])

(defn view
  []
  (let [listings        @(rf/subscribe [:filtered-projects])
        tags            @(rf/subscribe [:project-tags])
        selected-tags   @(rf/subscribe [:key :forms :projects :search :data :tags])
        {:keys [input]} (stfc/form [:projects :search])]
    [:div.list
     [:div.intro
      [:div.main
       [:p "Looking to improve your skills and work with real code? These projects are under active development and welcome new contributors."]]
      [:div.secondary
       [:button.submit.target "Post Your Project"]]]
     [:div.main.listings.public
      [ui/ctg {:transitionName "filter-survivor" :class "listing-list"}
       (for [l listings]
         ^{:key (str "os-project-" (:slug l))}
         [:div.listing-container
          [:a.listing.clearfix {:href (:slug l)}
           [:div.core
            [:div.title [ui/attr l :project/name]]
            [ui/attr l :project/tagline]
            (if (:project/beginner-friendly l)
              [:div.beginner-friendly "beginner friendly"])
            (if-let [t (:record/tags l)]
              [:div.tags
               (for [tag (u/split-tags t)]
                 ^{:key (gensym)} [filter-tag selected-tags tag])])]]])]]
     [:div.secondary.listings
      [:div.section.search
       [input :search :query
        :no-label true
        :placeholder "Search: `music`, `database` ..."]]
      [:div.section.beginner-toggle
       [input :checkbox :project/beginner-friendly :label "Beginner friendly?"]]
      [:div.section.tags
       [:div
        (for [tag tags]
          ^{:key (gensym)} [filter-tag selected-tags tag])]]
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
         "!"]]]]]))
