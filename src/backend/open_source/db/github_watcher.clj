(ns open-source.db.github-watcher
  (:require [integrant.core :as ig]
            [open-source.db.github :as osgh]
            [clojure.core.async :refer (go-loop >!! close! chan timeout alts!)]
            [taoensso.timbre :as timbre]))



(defmethod ig/init-key :open-source.db/github-watcher [_ {:keys [replace-interval refresh-interval db]}]
  (let [kill-ch (chan)]
    (go-loop []
      (when-not (= kill-ch (second (alts! [kill-ch (timeout refresh-interval)])))
        (timbre/info "watcher refreshing projects")
        (osgh/refresh-projects! db)
        (recur)))
    (go-loop []
      (when-not (= kill-ch (second (alts! [kill-ch (timeout replace-interval)])))
        (timbre/info "watcher replacing projects")
        (osgh/replace-local-projects! db)
        (recur)))
    kill-ch))

(defmethod ig/halt-key! :open-source.db/github-watcher [_ watcher]
  (>!! watcher :stop))
