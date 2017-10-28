(ns open-source.routes)

(def routes ["/" {"" :project-list
                  
                  ["projects/" :project-id]
                  {"" :show-project
                   "/edit" :edit-project}}])
