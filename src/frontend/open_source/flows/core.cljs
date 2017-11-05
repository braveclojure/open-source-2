(ns open-source.flows.core
  (:require [re-frame.core :as rf]
            [accountant.core :as acc]))

(rf/reg-event-db :init
  (fn [db _] {}))

(rf/reg-fx :nav
  (fn [path]
    (acc/navigate! path)))
