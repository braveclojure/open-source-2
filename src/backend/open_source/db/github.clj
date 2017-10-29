(ns open-source.db.github
  (:require [integrant.core :as ig]
            [clojure.string :as str]
            [clojure.set :as set]
            [clj-http.client :as client]
            [clojure.edn :as edn]
            [cheshire.core :as json]))

(def projects (atom {}))

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
  [project file]
  (merge project
         (select-keys file [:sha :path])
         {:slug  (-> file :path slug)
          :db/id (id project)}))

(defn api-headers
  [auth-token]
  {:as :json
   :headers {"Authorization" (str "token " auth-token)}})

(defn filter-updated-files
  "All github files whose sha doesn't match the corresponding project"
  [current-projects files]
  (filter (fn [file] (not= (get-in current-projects [(:path file) :sha])
                           (:sha file)))
          files))

(defn filter-deleted-file-paths
  "Paths of projects that don't exist in github repo"
  [current-projects files]
  (set/difference (set (keys current-projects))
                  (set (map :path files))))

(defn read-project-index
  [user repo auth-token]
  (:body (client/get (format "https://api.github.com/repos/%s/%s/contents/projects" user repo)
                     (api-headers auth-token))))

(defn read-projects
  [auth-token files]
  (pmap (fn [file]
          (-> (:download_url file)
              client/get
              :body
              edn/read-string
              (add-metadata file)))
        files))

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
       (reduce (fn [xs x] (assoc xs (:path x) x))
               current-projects)
       (#(apply dissoc % deleted-file-paths))))


(defn refresh-projects
  [current-projects user repo auth-token]
  (let [project-index (read-project-index user repo auth-token)]
    (update-projects current-projects
                     (get-updated-files auth-token current-projects project-index)
                     (filter-deleted-file-paths current-projects project-index))))

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
  (client/put (format "https://api.github.com/repos/%s/%s/contents/projects/%s" user repo (str (id project) ".edn"))
              (merge (api-headers auth-token)
                     {:body (json/generate-string (github-project-params project))})))

(defprotocol GithubProjectDb
  "Interact with Project"
  (refresh-projects! [project-db])
  (write-project! [project-db project])
  (project-list [project-db]))

(defrecord ProjectDb [project-atom user repo auth-token]
  GithubProjectDb
  (refresh-projects! [_]
    (swap! project-atom refresh-projects user repo auth-token))
  (write-project! [_ project]
    (write-project-to-github project user repo auth-token)
    (swap! project-atom assoc (:db/id project) project))
  (project-list [_]
    (vals @project-atom)))

(defmethod ig/init-key :open-source.db/github [_ {:keys [user repo auth-token] :as github-config}]
  (let [project-db (map->ProjectDb (assoc github-config :project-atom projects))]
    (refresh-projects! project-db)
    project-db))
