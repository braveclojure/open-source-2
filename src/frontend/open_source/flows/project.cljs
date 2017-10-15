(ns open-source.flows.project
  (:require [re-frame.core :as rf]
            [sweet-tooth.frontend.pagination.flow :as stpf]))

(rf/reg-sub :projects
  (fn [db _]
    (get-in db [:entity :project])))

(rf/reg-event-fx :load-projects
  [rf/trim-v]
  (stpf/GET-page-fx "/api/projects" {}))
