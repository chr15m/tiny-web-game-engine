(ns twge
  (:require
    [promesa.core :as p]
    [applied-science.js-interop :as j]
    ["preact" :refer [h render]]))

(defn ^:export timeout [ms]
  (p/delay ms))

(defn load-image [url]
  (js/Promise.
    (fn [res err]
      (let [i (js/Image.)]
        (j/assoc! i "onload" (res i))
        (j/assoc! i "onerror" err)
        (j/assoc! i "src" url)))))

(defn image [url]
  (p/let [i (load-image url)]
    (h "img" #js {:src (j/get i :src)
                  :class "twge-entity"})))

(defn ^:export draw [entities]
  (let [app (h "div" nil entities)
        el (.getElementById js/document "twge-default")]
    (j/assoc! el "innerHTML" "")
    (render app el)))
