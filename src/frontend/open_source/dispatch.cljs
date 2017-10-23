(ns open-source.dispatch
  (:require [secretary.core :as secretary :refer-macros [defroute]]
            [re-frame.core :refer [dispatch]]
            [accountant.core :as acc]
            [bidi.bidi :as bidi]
            [clojure.string :as str]
            [taoensso.timbre :as timbre]

            [open-source.routes :as routes]
            [open-source.components.project.list :as pl]
            [open-source.components.project.show :as ps]
            
            [sweet-tooth.frontend.core.utils :as stcu]
            [sweet-tooth.frontend.routes.flow :as strf]
            [sweet-tooth.frontend.routes.utils :as stru]))

(defmulti dispatch-route (fn [handler params] handler))

(defmethod dispatch-route
  :project-list
  [handler params]
  (dispatch [:load-projects])
  (dispatch [::strf/load :project-list [pl/component] params]))

(defmethod dispatch-route
  :show-project
  [handler params]
  (dispatch [:load-projects])
  (dispatch [::strf/load :show-project [ps/component] params]))

(defmethod dispatch-route
  :default
  [handler params]
  (timbre/error "No route handler for" handler params))

(defonce nav
  ;; defonce to prevent this from getting re-configured with
  ;; boot/reload on every change
  (acc/configure-navigation!
    {:nav-handler
     (fn [path]
       (let [match (bidi/match-route routes/routes path)]
         (dispatch-route (:handler match)
                         (merge (:route-params match)
                                (stru/query-params path)))))
     :path-exists?
     (fn [path]
       (boolean (bidi/match-route routes/routes path)))}))
