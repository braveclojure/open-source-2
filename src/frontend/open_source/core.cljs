(ns open-source.core
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe]]
            [accountant.core :as acc]
            [open-source.dispatch]
            [open-source.handlers]
            [open-source.subs]
            [sweet-tooth.frontend.core.utils :as stcu]
            [sweet-tooth.frontend.routes.flow :as strf]))

(enable-console-print!)

;; treat node lists as seqs; not related to the rest
(extend-protocol ISeqable
  js/NodeList
  (-seq [node-list] (array-seq node-list))

  js/HTMLCollection
  (-seq [node-list] (array-seq node-list)))


(defn app
  []
  [:div.app @(subscribe [::strf/routed-component])])

(defn -main []
  (r/render [app] (stcu/el-by-id "app"))
  (acc/dispatch-current!))

(-main)
