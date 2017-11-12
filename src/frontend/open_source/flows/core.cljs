(ns open-source.flows.core
  (:require [re-frame.core :as rf]
            [accountant.core :as acc]
            [sweet-tooth.frontend.paths :as stfp]
            [open-source.flows.project :as project-flow]))

(rf/reg-event-db :init
  (fn [db _]
    ;; default the 'most days since last push' filter to 365
    ;; cause if it hasn't had activity in a year it's probably less relevant
    (-> {}
        (assoc-in (stfp/full-form-path project-flow/filter-form-path :data :days-since-push) 365)
        (assoc-in [:ui :project :list :sort] {:attr :slug :dir :desc}))))

(rf/reg-fx :nav
  (fn [path]
    (acc/navigate! path)))
