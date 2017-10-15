(ns open-source.flows.core
  (:require [re-frame.core :as rf]))

(def canned-projects
  [{:project/name "Afterglow",
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
    "music, audio, art, language design, programming language, music programming"}])

(rf/reg-event-db :init
  (fn [db _] {:data {:projects canned-projects}}))
