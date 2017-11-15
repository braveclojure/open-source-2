(ns open-source.flows.project
  (:require [re-frame.core :as rf]
            [open-source.utils :as u]
            [clojure.set :as set]
            [sweet-tooth.frontend.remote.flow :as strf]
            [sweet-tooth.frontend.routes.flow :as strof]
            [sweet-tooth.frontend.filter.flow :as stfilterf]
            [sweet-tooth.frontend.form.flow :as stff]
            [sweet-tooth.frontend.form.utils :as stfu]
            [sweet-tooth.frontend.core.utils :as stcu]
            [sweet-tooth.frontend.paths :as p]))

(rf/reg-sub ::raw-projects
  (fn [db _] (get-in db [:entity :project])))

(rf/reg-sub ::projects
  :<- [::raw-projects]
  (fn [raw-projects _] (->> raw-projects
                            vals
                            (map (fn [p] (assoc p :record/split-tags (->> (:record/tags p)
                                                                          u/split-tags
                                                                          (filter (complement empty?))
                                                                          set)))))))

(rf/reg-sub ::tags
  :<- [::projects]
  (fn [projects _]
    (distinct (mapcat :record/split-tags projects))))

(rf/reg-sub ::current-project
  :<- [::raw-projects]
  :<- [::strof/nav]
  (fn [[raw-projects nav] _]
    (get raw-projects (:project-id (:params nav)))))

(rf/reg-event-fx :load-projects
  [rf/trim-v]
  (fn [{:keys [db] :as cofx} args]
    ;; poor man's caching
    ;; use etags or last modified you silly goose
    (if-not (empty? (get-in db [:entity :project]))
      {}
      ((strf/GET-list-fx "/api/project") cofx args))))

;;===========
;; Editing a project
;;===========

(defn project-id
  [db]
  (get-in db [p/nav-prefix :params :project-id]))

(defn copy-project-for-edit
  [db]
  (let [project-id (project-id db)]
    (assoc-in db
              (into (p/full-form-path [:project :update project-id])
                    [:data])
              (get-in db [p/entity-prefix :project project-id]))))

(rf/reg-event-db :edit-project-load-success
  [rf/trim-v]
  (fn [db [m project-id]]
    (copy-project-for-edit (stcu/deep-merge db m))))

(rf/reg-event-fx :edit-project
  [rf/trim-v]
  (fn [{:keys [db] :as cofx} args]
    ;; poor man's caching
    ;; use etags or last modified you silly goose
    (if-not (empty? (get-in db [:entity :project]))
      {:db (copy-project-for-edit db)}
      ((strf/GET-list-fx "/api/project" {:on-success [:edit-project-load-success (project-id db)]}) cofx args))))


;;===========
;; Create a project
;;===========

(rf/reg-event-fx ::created-project
  [rf/trim-v]
  (fn [{:keys [db]} args]
    {:db  (stff/submit-form-success db args)
     :nav "/"}))

;;===========
;; Filter projects
;;===========
(def filter-form-path [:projects :filter])

(defn tag-filter
  [_ selected-tags _ projects]
  (if (seq selected-tags)
    (filter (fn [project]
              (= (set/intersection selected-tags (:record/split-tags project))
                 selected-tags))
            projects)
    projects))

(defn inclusive-filter-attr<=
  [form-attr attr-val [key-fn] xs]
  (if attr-val
    (let [key-fn (or key-fn form-attr)]
      (filter #(let [x-val (key-fn %)]
                 (or (nil? x-val)
                     (<= x-val attr-val)))
              xs))
    xs))

(stfilterf/reg-filtered-sub ::filtered-projects
                            ::projects
                            filter-form-path
                            [[:project/beginner-friendly stfilterf/filter-toggle]
                             [:query stfilterf/filter-query]
                             [:selected-tags tag-filter]
                             [:stargazers-count stfilterf/filter-attr>= #(get-in % [:project/stats :stargazers-count])]
                             [:days-since-push inclusive-filter-attr<= #(get-in % [:project/stats :days-since-push])]])

(rf/reg-event-db ::toggle-tag
  [rf/trim-v]
  (fn [db [tag]]
    (stfu/update-in-form db
                         filter-form-path
                         :selected-tags
                         (fn [selected-tags]
                           (if (nil? selected-tags)
                             #{tag}
                             (if (selected-tags tag)
                               (disj selected-tags tag)
                               (conj selected-tags tag)))))))

;;===========
;; Sort projects
;;===========

(rf/reg-sub ::sort
  (fn [db _]
    (get-in db [:ui :project :list :sort])))

(rf/reg-event-db ::set-sort
  [rf/trim-v]
  (fn [db [sort-attr]]
    (update-in db [:ui :project :list :sort]
               (fn [{:keys [attr dir]}]
                 {:attr sort-attr
                  :dir  ({true :desc false :asc}
                         (or (not= attr sort-attr)
                             (not= dir :desc)))}))))

;;===========
;; Sorted, filtered project
;;===========

(rf/reg-sub ::project-list
  :<- [::sort]
  :<- [::filtered-projects]
  (fn [[{:keys [attr dir]} projects] _]
    (let [key-fn (if (= :slug attr)
                   :slug
                   #(get-in % [:project/stats attr]))]
      (sort-by key-fn (if (= :desc dir) < >) projects))))
