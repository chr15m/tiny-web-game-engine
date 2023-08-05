(ns twge
  (:require
    [promesa.core :as p]
    [applied-science.js-interop :as j]
    ["hyperscript" :as h]))

; collisions: https://stackoverflow.com/a/19614185

(defn timeout [ms]
  (p/delay ms))

(defn load-image [url]
  (js/Promise.
    (fn [res err]
      (let [i (js/Image.)]
        (j/assoc! i "onload" #(res i))
        (j/assoc! i "onerror" err)
        (j/assoc! i "src" url)))))

(defn default-unit [t v]
  (let [unit (cond (not= (.indexOf (j/lit [:x :y :w :h]) t) -1) "px")]
    (if (or (= (type v) js/Number)
            (= (str (js/parseFloat v)) v))
      (str v unit)
      v)))

(defn get-style [props]
  (.reduce
    (j/lit [:x :y :w :h])
    (fn [style k]
      (j/assoc! style
                (str "--" k)
                (default-unit k
                  (j/get props k))))
    #js {}))

(defn image [url & [props]]
  (p/let [i (load-image url)]
    (let [style (when props (get-style props))
          el (h "img" (j/lit {:src (j/get i :src)
                              :className "twge-entity"
                              :style style}))
          set-fn (fn [props]
                   (let [tmp (h "img" (j/lit {:style (get-style props)}))]
                     ; TODO: is there a faster way to copy styles than text conversion?
                     ; https://github.com/hyperhype/hyperscript/blob/master/index.js#L82-L98
                     (j/assoc! el :style (j/get-in tmp [:style :cssText]))))]
      (j/lit
        {:element el
         :set set-fn}))))

(def root (.getElementById js/document "twge-default"))

(def scene
  (j/lit
    {:new (fn [alternate-root]
            (let [r (or alternate-root root)]
              (j/assoc! r :innerHTML "")
              (j/lit {:root r})))
     :add (fn [scene entity]
            (j/call (j/get scene :root)
                    :appendChild
                    (j/get entity :element)))}))
