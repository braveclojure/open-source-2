(ns open-source.subs
  (:require [re-frame.core :refer [reg-sub trim-v]]
            [sweet-tooth.frontend.core.utils :as stcu]))

(defn param
  [db & path]
  (get-in db (into [:nav :params] path)))

;; re-frame docs say this is a bad idea
;; well, my middle name is a bad idea
