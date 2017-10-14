(ns open-source.components.ui
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [clojure.string :as s]
            [sweet-tooth.frontend.core.utils :as stcu]
            [sweet-tooth.frontend.paths :as paths]
            [sweet-tooth.frontend.core.flow :as stcf]
            [goog.events.KeyCodes :as KeyCodes]))

;; TODO these belong in a library

;; common components
(defn toggle-btn
  ([visible show-text hide-text]
   (toggle-btn visible show-text hide-text #(swap! visible not)))
  ([visible show-text hide-text on-click]
   (let [vis @visible
         classname (if vis "hide" "show")
         text      (str " " (if vis hide-text show-text))
         i-class   (if vis "fa-minus-circle" "fa-plus-circle")]
     [:div.toggle-btn
      [:span {:class classname
              :on-click on-click}
       [:i {:class (str "fa " i-class)}]
       text]])))

(defn form-toggle
  [form-path show-text hide-text & [data]]
  (let [full-form-path (paths/full-form-path form-path)
        ui-state-path (conj full-form-path :ui-state)
        visible (subscribe (stcu/flatv :key ui-state-path))
        toggle-fn #(dispatch [::stcf/toggle ui-state-path])]
    (toggle-btn visible
                show-text
                hide-text
                (if data
                  #(do (dispatch [::stcf/assoc-in (stcu/flatv full-form-path :data) data])
                       (toggle-fn))
                  toggle-fn))))

(defn focus-child
  [component tag-name & [timeout]]
  (with-meta (fn [] component)
    {:component-did-mount
     (fn [el]
       (let [node (first (.getElementsByTagName (r/dom-node el) tag-name))]
         (if timeout
           (js/setTimeout #(.focus node) timeout)
           (.focus node))))}))

(defn on-esc
  [f]
  (fn [e]
    (when (= KeyCodes/ESC (.-keyCode e))
      (f))))

;; transition group
(def rtg (r/adapt-react-class (-> js/React (aget "addons" "TransitionGroup"))))
(def ctg (r/adapt-react-class (-> js/React (aget "addons" "CSSTransitionGroup"))))
