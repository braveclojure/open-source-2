(ns open-source.handlers
  (:require [ajax.core :refer [GET]]
            [re-frame.core :refer [reg-event-fx trim-v]]
            [secretary.core :as secretary]
            [sweet-tooth.frontend.core.flow :as stcf]
            [sweet-tooth.frontend.form.flow :as stff]
            [sweet-tooth.frontend.remote.flow :as strf]
            [sweet-tooth.frontend.pagination.flow :as stpf]))

(defmethod stff/url-prefix :default [_ _] "/api/v1")
(defmethod stff/data-id :default [_ _ data] (:db/id data))

;; initialize the handler with no interceptors
;; if you wanted to add headers to every HTTP request, you'd do it here
(strf/reg-http-event-fx [])
