(ns open-source.flows.core
  (:require [re-frame.core :as rf]))

(rf/reg-event-db :init
  (fn [db _] {}))
