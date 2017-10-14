(set-env!
  :source-paths #{"src/backend" "src/frontend" "dev/src"}
  :resource-paths #{"resources" "dev/resources"}
  :dependencies '[[org.clojure/clojure "1.9.0-alpha16"]
                  [org.clojure/clojurescript "1.9.854"]
                  [adzerk/boot-cljs "RELEASE" :scope "test"]
                  [com.taoensso/timbre "4.10.0"]

                  [org.clojure/tools.logging "0.3.1"]
                  [ring "1.5.0" :exclusions [org.clojure/tools.namespace]]
                  [ring-jetty-component "0.3.1"]
                  [liberator "0.14.1"]
                  [com.flyingmachine/liberator-unbound "0.1.1"]
                  [com.flyingmachine/webutils "0.1.6"]
                  [compojure "1.5.0"]
                  [medley "0.7.1"]
                  [clj-time "0.11.0"]
                  [cheshire "5.6.2"]

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

                  ;; duct
                  [duct/core "0.5.0"]
                  [duct/module.logging "0.2.0"]
                  [duct/module.web "0.5.4"]
                  [integrant "0.4.1"]

                  ;; local dev
                  [integrant/repl "0.2.0" :scope "test"]

                  ;; sweet tooth
                  [sweet-tooth/sweet-tooth-frontend "0.2.5"]
                  [sweet-tooth/sweet-tooth-endpoint "0.2.1"]
                  [sweet-tooth/sweet-tooth-workflow "0.2.1"]])

(require '[boot.core]
         '[adzerk.boot-cljs :refer [cljs]]
         '[adzerk.boot-reload :refer [reload]]
         '[sweet-tooth.workflow.tasks :refer [dev reload-integrant] :as tasks]
         '[integrant.repl :as ir]
         '[dev])

(task-options!
  cljs {:compiler-options {:asset-path "/main.out"
                             :parallel-build true
                             :preloads '[devtools.preload]}}

  reload {:on-jsload 'open-source.core/-main}

  reload-integrant {:prep-fn 'dev/prep})
