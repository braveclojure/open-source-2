(ns open-source.flows.project
  (:require [re-frame.core :as rf]))

(rf/reg-sub :projects
  (fn [db _]
    (get-in db [:data :projects])))
