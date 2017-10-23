(ns open-source.routes)

(def routes ["/" {"" :project-list
                  ["project/" :project-id] :show-project}])
