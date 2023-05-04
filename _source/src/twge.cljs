(ns twge
  (:require
    [promesa.core :as p]
    [applied-science.js-interop :as j]
    ["preact" :refer [h render]]))

; collisions: https://stackoverflow.com/a/19614185

(defn ^:export timeout [ms]
  (p/delay ms))

(defn load-image [url]
  (js/Promise.
    (fn [res err]
      (let [i (js/Image.)]
        (j/assoc! i "onload" #(res i))
        (j/assoc! i "onerror" err)
        (j/assoc! i "src" url)))))

(defn image [url]
  (p/let [i (load-image url)]
    (fn [props]
      (h "img" #js {:src (j/get i :src)
                    :class "twge-entity"
                    :style #js {"--x" (j/get props :x)
                                "--y" (j/get props :y)}}))))

(defn ^:export draw [entities]
  (let [app (h "div" nil entities)
        el (.getElementById js/document "twge-default")]
    (j/assoc! el "innerHTML" "")
    (render app el)))
