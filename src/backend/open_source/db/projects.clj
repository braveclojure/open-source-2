(ns open-source.db.projects
  #_(:require [clojure.core.async :refer (go >!! close! chan timeout alts!)]
            [com.stuartsierra.component :as component]
            [open-source.db.github :as gh]))

(comment
  (def projects (atom {}))

  (defprotocol ProjectWatcher
    "Periodically pull data from github to ensure projects are up to date"
    (start [x] "Start watching")
    (stop  [x] "Stop watching"))


  (defn watcher
    [projects interval kill-ch]
    (swap! projects gh/refresh-projects)
    (let [timeout-ch (atom (timeout interval))]
      (go (while (= (second (alts! [kill-ch @timeout-ch])) @timeout-ch)
            (swap! projects gh/refresh-projects)
            (reset! timeout-ch (timeout interval))))))

  (defrecord CoreAsyncProjectWatcher [projects interval kill-ch]
    ProjectWatcher
    (start [this]
      (when-not @kill-ch
        (reset! kill-ch (chan))
        (watcher projects interval @kill-ch)))
    (stop [this]
      (>!! @kill-ch :stop)
      (close! @kill-ch)
      (reset! kill-ch nil)))


  (defn new-core-async-project-watcher
    [projects interval]
    (map->CoreAsyncProjectWatcher {:projects projects
                                   :interval interval
                                   :kill-ch  (atom nil)}))

  (defrecord ProjectDb [project-watcher]
    component/Lifecycle
    (start [component]
      (start project-watcher)
      (assoc component :project-watcher project-watcher))

    (stop [component]
      (stop project-watcher)
      (assoc component :project-watcher nil)))

  (defn new-project-db
    [interval]
    (map->ProjectDb {:project-watcher (new-core-async-project-watcher projects interval)}))
  )
