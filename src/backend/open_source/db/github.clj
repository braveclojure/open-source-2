(ns open-source.db.github
  (:require [clojure.string :as str]
            [clojure.set :as set]
            [clj-http.client :as client]
            [clojure.edn :as edn]))

(def projects (atom [{:project/name "Afterglow",
                      :project/tagline
                      "Live-coding algorithmic light shows for DMX and other protocols",
                      :project/repo-url "https://github.com/brunchboy/afterglow",
                      :project/home-page-url "https://github.com/brunchboy/afterglow",
                      :project/description
                      "An environment supporting live coding for the creation of algorithmic light shows in Clojure, leveraging the Open Lighting Architecture with the help of ola-clojure, wayang, beat-link, and pieces of the Overtone toolkit. Beyond building on pieces of Overtone, the entire Afterglow project was inspired by it.",
                      :record/tags "music, lighting, DMX, MIDI, OSC"}
                     {:project/name "alda",
                      :project/tagline "A general purpose music programming language",
                      :project/repo-url "https://github.com/alda-lang/alda",
                      :project/home-page-url "http://alda.io",
                      :project/beginner-issues-label "low-hanging fruit",
                      :project/description
                      "Alda is a general purpose music programming language designed to be a flexible and powerful way to create music by writing code. The language is designed with a simple, Markdown-like syntax that can be picked up easily by musicians with little-to-no programming experience. There is support for writing Clojure code inline in an Alda score, allowing Clojure programmers to write algorithms that generate music.\n\nAlda currently allows you to create MIDI music, and there are plans to support a number of other exciting things like:\n\n* building synthesizer instruments via waveform synthesis\n* generating sheet music\n* extending the Alda syntax via a plugin system\n* importing and editing MIDI files\n\nThe project is still relatively young and this is an exciting time to contribute to its development. If you're interested in contributing, feel free to take a look at the open issues on GitHub and pick up any that interest you. You can also stop by our Slack chat group at http://slack.alda.io and say hello!",
                      :project/beginner-friendly true,
                      :record/tags
                      "music, audio, art, language design, programming language, music programming"}]))

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
  (str/replace #".edn$" ""))

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
              (client/get (api-headers auth-token))
              :body
              edn/read-string
              (add-metadata file)))
        files))

(defn get-updated-files
  "Finds all project files that have been updated, reads them, adds
  project metadata"
  [current-projects project-index]
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
                     (get-updated-files current-projects project-index)
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
  (sorted-map-by (fn [x y] (< (.indexOf project-keys x) (.indexOf project-keys y)))))

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
  (with-out-str
    (as-> project $
      (select-keys $ project-keys)
      (ensure-http $ [:project/repo-url :project/home-page-url])
      (into template $)
      (clojure.pprint/pprint $))))

(defn write-project-to-github
  [project user repo auth-token]
  ;; TODO update this
  #_(r/update-contents user
                     repo
                     (str "projects/" (slugify (:project/name project)) ".edn")
                     "updating project via web"
                     (project-file-body project)
                     (:sha project)
                     (oauth-token)))

(defn write-project!
  [projects project]
  (let [path (str "projects/" (slugify (:project/name project)) ".edn")
        result (:content (write-project-to-github project))]
    (swap! projects assoc path (merge-github-data result project))
    @projects))
