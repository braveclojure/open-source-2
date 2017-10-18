(ns open-source.flows.project
  (:require [re-frame.core :as rf]
            [sweet-tooth.frontend.remote.flow :as strf]))

(rf/reg-sub :projects
  (fn [db _] (vals (get-in db [:entity :project]))))

(rf/reg-event-fx :load-projects
  [rf/trim-v]
  (fn [{:keys [db] :as cofx} args]
    (if (seq (vals (get-in db [:entity :project])))
      {}
      ((strf/GET-list-fx "/api/projects") cofx args))))
