(ns open-source.flows.project
  (:require [re-frame.core :as rf]
            [sweet-tooth.frontend.remote.flow :as strf]))

(rf/reg-sub :projects
  (fn [db _]
    (let [x (vals (get-in db [:entity :project]))]
      (pr "projects" x)
      x)))

(rf/reg-event-fx :load-projects
  [rf/trim-v]
  (strf/GET-list-fx "/api/projects"))
