(ns open-source.flows.project
  (:require [re-frame.core :as rf]
            [sweet-tooth.frontend.remote.flow :as strf]
            [sweet-tooth.frontend.routes.flow :as strof]))

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
    (if (seq (vals (get-in db [:entity :project])))
      {}
      ((strf/GET-list-fx "/api/projects") cofx args))))
