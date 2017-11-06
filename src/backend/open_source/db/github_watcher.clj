(ns open-source.db.github-watcher
  (:require [integrant.core :as ig]
            [open-source.db.github :as osgh]
            [clojure.core.async :refer (go-loop >!! close! chan timeout alts!)]))



(defmethod ig/init-key :open-source.db/github-watcher [_ {:keys [interval db]}]
  (let [kill-ch (chan)]
    (go-loop []
      (when-not (= kill-ch (second (alts! [kill-ch (timeout interval)])))
        (osgh/refresh-projects! db)
        (recur)))
    kill-ch))

(defmethod ig/halt-key! :open-source.db/github-watcher [_ watcher]
  (>!! watcher :stop))