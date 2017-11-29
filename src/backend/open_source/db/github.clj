(ns open-source.db.github
  (:require [integrant.core :as ig]
            [clojure.string :as str]
            [clojure.set :as set]
            [clj-http.client :as client]
            [clojure.edn :as edn]
            [cheshire.core :as json]
            [clj-time.core :as t]
            [clj-time.format :as tf]
            [clj-time.local :as tl]
            [taoensso.timbre :as timbre]))

(def projects (atom {}))
(def etags (atom {}))
(def responses (atom {}))

(defn github-headers
  [auth-token & [etag as-json]]
  (cond-> {:headers (cond-> {"Authorization" (str "token " auth-token)}
                      etag (assoc "If-None-Match" etag))}
    as-json (assoc :as :json)))

(defn github-url
  [user repo endpoint]
  (format "https://api.github.com/repos/%s/%s%s" user repo endpoint))

(defn github-get
  [url auth-token & [as-json]]
  (try (let [etag     (get @etags url)
             response (client/get url (github-headers auth-token etag as-json))]
         (if (= (:status response) 304)
           (get @responses etag)
           (let [new-etag (get-in response [:headers "ETag"])
                 body (:body response)]
             (swap! etags assoc url new-etag)
             (swap! responses assoc new-etag body)
             body)))
       (catch Exception e
         (timbre/error "Exception getting from API" (.getMessage e)
                       url
                       (ex-data e)))))

(defn format-time
  [t]
  (tf/parse (tf/formatters :date-time-no-ms) t))

(defn days-old
  [t]
  (-> t
      format-time
      (t/interval (clj-time.local/local-now))
      t/in-days))

(defn slugify
  "Take arbitrary text and format it for readable url"
  [txt]
  (-> txt
      str/lower-case
      (str/replace #"[^a-zA-Z0-9]" "-")
      (str/replace #"-+" "-")
      (str/replace #"-$" "")))

(defn id
  [project]
  (-> project :project/name slugify))

(defn slug
  [path]
  (str/replace path #".edn$" ""))

(defn add-metadata
  "Derive some values from the base data in a project file"
  [project file]
  (merge project
         (select-keys file [:sha :path])
         {:slug  (-> file :path slug)
          :db/id (id project)}))

(defn read-project-stats
  "If the project's on github, use the API to read useful stats that
  indicate activity and popularity"
  [project auth-token]
  (if-let [[_ user repo] (re-find #"https?://github.com/([^/]+)/([^/]+)/?$" (:project/repo-url project))]
    (if-let [stats (github-get (github-url user repo "") auth-token :as-json)]
      (assoc project :project/stats {:stargazers-count (:stargazers_count stats)
                                     :pushed-at        (:pushed_at stats)
                                     :days-since-push  (days-old (:pushed_at stats))}))
    project))

(defn filter-updated-files
  "All github files whose sha doesn't match the corresponding project"
  [current-projects files]
  (filter (fn [file] (not= (get-in current-projects [(:path file) :sha])
                           (:sha file)))
          files))

(defn filter-deleted-file-paths
  "Paths of projects that don't exist in github repo"
  [current-projects files]
  (set/difference (set (map :path (vals current-projects)))
                  (set (map :path files))))

(defn read-project-index
  [user repo auth-token]
  (github-get (github-url user repo "/contents/projects") auth-token :as-json))

(defn read-project
  [auth-token file]
  (some-> (:download_url file)
          (github-get auth-token)
          edn/read-string
          (add-metadata file)
          (read-project-stats auth-token)))

(defn read-projects
  "Download project files"
  [auth-token files]
  (pmap (partial read-project auth-token) files))

(defn get-updated-files
  "Finds all project files that have been updated, reads them, adds
  project metadata"
  [auth-token current-projects project-index]
  (->> project-index
       (filter-updated-files current-projects)
       (read-projects auth-token)))

(defn update-projects
  "Resolves the differences between the current projects and github files"
  [current-projects updated-files deleted-file-paths]
  (->> updated-files
       (reduce (fn [xs x] (assoc xs (:db/id x) x))
               current-projects)
       (remove (fn [[k v]] (deleted-file-paths (:path v))))
       (into {})))


(defn refresh-projects
  "Ensures that the .edn files and memory representation of projects
  are in sync."
  [current-projects user repo auth-token]
  (if-let [project-index (read-project-index user repo auth-token)]
    (update-projects current-projects
                     (get-updated-files auth-token current-projects project-index)
                     (filter-deleted-file-paths current-projects project-index))
    current-projects))

(defn replace-local-projects
  "Replaces local with remote, ensuring every project gets
  updated. Easy way to get updated GH stats on each project."
  [_current-projects user repo auth-token]
  (let [project-index (read-project-index user repo auth-token)]
    (update-projects {}
                     (get-updated-files auth-token {} project-index)
                     (filter-deleted-file-paths {} project-index))))

;; ------
;; update the project db
;; ------

(def project-keys
  "Used to ensure the project gets printed to a file with the keys in
  the right order"
  [:project/name
   :project/tagline
   :project/repo-url
   :project/home-page-url 
   :project/beginner-issues-label
   :project/description
   :project/beginner-friendly
   :record/tags])

(def template
  "Sorted map that ensures keys get printed in correct order"
  (sorted-map-by (fn [x y]
                   (< (.indexOf project-keys x)
                      (.indexOf project-keys y)))))

(defn add-http
  [url]
  (when url
    (if (re-find #"^http" url)
      url
      (str "http://" url))))

(defn ensure-http
  [x url-keys]
  (reduce (fn [m k] (clojure.core/update m k add-http))
          x
          url-keys))


(defn project-file-body
  [project]
  (binding [clojure.core/*print-namespace-maps* false]
    (with-out-str
      (as-> project $
        (select-keys $ project-keys)
        (ensure-http $ [:project/repo-url :project/home-page-url])
        (into template $)
        (clojure.pprint/pprint $)))))

(defn encode64
  [s]
  (.encodeToString (java.util.Base64/getEncoder)
                   (.getBytes s)))

(defn github-project-params
  [{:keys [:project/name :sha] :as project}]
  (cond-> {:message (str "updating " name " via web")
           :content (encode64 (project-file-body project))}
    sha (assoc :sha sha)))

(defn write-project-to-github
  [project user repo auth-token]
  (client/put (github-url user repo (str "/contents/projects/" (str (id project) ".edn")))
              (merge (github-headers auth-token nil :as-json)
                     {:body (json/generate-string (github-project-params project))})))

(defn cache-projects
  [projects]
  (when-not (empty? projects) (spit "projects.edn" (str projects))))

(defn read-cache
  []
  (try (read-string (slurp "projects.edn"))
       (catch Exception e nil)))

(defn swap-and-cache!
  [& args]
  (let [new (apply swap! args)]
    (cache-projects new)
    new))

(defprotocol GithubProjectDb
  "Interact with Project"
  (refresh-projects! [project-db])
  (replace-local-projects! [project-db])
  (write-project! [project-db project])
  (project-list [project-db]))

(defrecord ProjectDb [project-atom user repo auth-token]
  GithubProjectDb
  (replace-local-projects! [_]
    (swap-and-cache! project-atom replace-local-projects user repo auth-token))
  (refresh-projects! [_]
    (swap-and-cache! project-atom refresh-projects user repo auth-token))
  (write-project! [project-db project]
    (write-project-to-github project user repo auth-token)
    (refresh-projects! project-db))
  (project-list [_]
    (or (vals @project-atom) [])))

(defmethod ig/init-key :open-source.db/github [_ {:keys [user repo auth-token] :as github-config}]
  (reset! projects (read-cache))
  (let [project-db (map->ProjectDb (assoc github-config :project-atom projects))]
    (refresh-projects! project-db)
    project-db))
