(set-env!
  :source-paths #{"src/backend" "src/frontend" "src/cross" "dev/src" "test/backend"}
  :resource-paths #{"resources" "dev/resources"}
  :dependencies '[[org.clojure/clojure "1.9.0"]
                  [org.clojure/clojurescript "1.10.520"]
                  [adzerk/boot-cljs "RELEASE" :scope "test"]
                  [com.taoensso/timbre "4.10.0"]
                  [org.clojure/core.async "0.4.500"]

                  [org.clojure/tools.logging "0.3.1"]
                  [ring "1.5.0" :exclusions [org.clojure/tools.namespace]]
                  [ring-jetty-component "0.3.1"]
                  [liberator "0.15.3"]
                  [com.flyingmachine/liberator-unbound "0.1.1"]
                  [com.flyingmachine/webutils "0.1.6"]
                  [compojure "1.5.0"]
                  [org.flatland/ordered "1.5.7"]
                  [medley "0.7.1"]
                  [clj-time "0.14.0"]
                  [cheshire "5.6.2"]
                  [tentacles "0.5.1"]
                  [hiccup "1.0.5"]
                  [me.raynes/cegdown "0.1.1"]
                  [clj-http "3.7.0"]

                  ;; TODO fix the fact that this is needed
                  [com.datomic/datomic-free "0.9.5344" :exclusions [com.google.guava/guava]]

                  ;; client
                  [reagent                     "0.7.0" :exclusions [cljsjs/react cljsjs/react-dom]]
                  [cljsjs/react-dom            "15.6.1-0" :exclusions [cljsjs/react]]
                  [cljsjs/react-with-addons    "15.6.1-0"]
                  [re-frame                    "0.10.1"]
                  [cljs-ajax                   "0.5.8"]
                  [secretary                   "1.2.3"]
                  [binaryage/devtools          "0.9.4"]
                  [venantius/accountant        "0.2.0"]
                  [bidi                        "2.1.1"]
                  [cljsjs/marked               "0.3.5-0"]

                  ;; duct
                  [duct/core "0.5.0"]
                  [duct/module.logging "0.2.0"]
                  [duct/module.web "0.5.4"]
                  [integrant "0.4.1"]

                  ;; local dev
                  [integrant/repl "0.2.0" :scope "test"]])

(def sweet-tooth-packages
  "Define this seperately so packages can get included as checkouts"
  '[[sweet-tooth/sweet-tooth-frontend "0.2.8"]
    [sweet-tooth/sweet-tooth-endpoint "0.2.2"]
    [sweet-tooth/sweet-tooth-workflow "0.2.4"]])

(set-env! :dependencies #(into % sweet-tooth-packages)
          ;; for dev
          ;; :checkouts sweet-tooth-packages
          )

(require '[boot.core]
         '[adzerk.boot-cljs :refer [cljs]]
         '[adzerk.boot-reload :refer [reload]]
         '[sweet-tooth.workflow.tasks :refer [dev reload-integrant build] :as tasks]
         '[integrant.repl :as ir]
         '[integrant.repl.state :as irs]
         '[dev])

(task-options!
  cljs {:compiler-options {:asset-path     "/main.out"
                           :parallel-build true
                           :preloads       '[devtools.preload]}}

  build {:version   "2.0.0"
         :project 'open-source
         :main    'open-source.core
         :file    "app.jar"}

  reload {:on-jsload 'open-source.core/-main}

  reload-integrant {:prep-fn 'dev/prep})
