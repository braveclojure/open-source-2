(ns open-source.dispatch
  (:require [secretary.core :as secretary :refer-macros [defroute]]
            [re-frame.core :refer [dispatch]]
            [accountant.core :as acc]
            [bidi.bidi :as bidi]
            [clojure.string :as str]

            [open-source.routes :as routes]
            [open-source.components.project.list :as pl]
            
            [sweet-tooth.frontend.core.utils :as stcu]
            [sweet-tooth.frontend.routes.flow :as strf]
            [sweet-tooth.frontend.routes.utils :as stru]))

(defmulti dispatch-route (fn [handler params] handler))

(defmethod dispatch-route
  :project-list
  [handler params]
  (dispatch [:load-projects])
  (dispatch [::strf/load :project-list [pl/component] params]))

(defonce nav
  ;; defonce to prevent this from getting re-configured with
  ;; boot/reload on every change
  (acc/configure-navigation!
    {:nav-handler
     (fn [path]
       (let [match (bidi/match-route routes/routes path)]
         (dispatch-route (:handler match) (merge (:route-params match) (stru/query-params path)))))
     :path-exists?
     (fn [path]
       (boolean (bidi/match-route routes/routes path)))}))
