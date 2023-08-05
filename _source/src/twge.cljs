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
      (let [style (.reduce
                    (j/lit [:x :y :w :h])
                    (fn [style k]
                      (j/assoc! style
                                (str "--" k)
                                (j/get props k)))
                    #js {})]
        (h "img" #js {:src (j/get i :src)
                      :class "twge-entity"
                      :style style})))))

(defn ^:export draw [entities]
  (let [app (h "div" nil entities)
        el (.getElementById js/document "twge-default")]
    (j/assoc! el "innerHTML" "")
    (render app el)))
