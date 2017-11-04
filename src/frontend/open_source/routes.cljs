(ns open-source.routes)

(def routes ["/" {"" :project-list
                  "projects/" {"new" :new-project
                               [:project-id] {"" :show-project
                                              "/edit" :edit-project}}}])
