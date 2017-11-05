(ns open-source.flows.project
  (:require [re-frame.core :as rf]
            [sweet-tooth.frontend.remote.flow :as strf]
            [sweet-tooth.frontend.routes.flow :as strof]
            [sweet-tooth.frontend.form.flow :as stff]
            [sweet-tooth.frontend.core.utils :as u]
            [sweet-tooth.frontend.paths :as p]))

(rf/reg-sub :projects
  (fn [db _] (get-in db [:entity :project])))

(rf/reg-sub :current-project
  :<- [:projects]
  :<- [::strof/nav]
  (fn [[projects nav] _]
    (get projects (:project-id (:params nav)))))

(rf/reg-event-fx :load-projects
  [rf/trim-v]
  (fn [{:keys [db] :as cofx} args]
    ;; poor man's caching
    ;; use etags or last modified you silly goose
    (if-not (empty? (get-in db [:entity :project]))
      {}
      ((strf/GET-list-fx "/api/project") cofx args))))

;; Editing a project
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
    (copy-project-for-edit (u/deep-merge db m))))

(rf/reg-event-fx :edit-project
  [rf/trim-v]
  (fn [{:keys [db] :as cofx} args]
    ;; poor man's caching
    ;; use etags or last modified you silly goose
    (if-not (empty? (get-in db [:entity :project]))
      {:db (copy-project-for-edit db)}
      ((strf/GET-list-fx "/api/project" {:on-success [:edit-project-load-success (project-id db)]}) cofx args))))


;; Create a project

(rf/reg-event-fx :created-project
  [rf/trim-v]
  (fn [{:keys [db]} args]
    {:db  (stff/submit-form-success db args)
     :nav "/"}))
