(ns twge
  (:require
    [promesa.core :as p]
    [applied-science.js-interop :as j]
    ["hyperscript" :as h]))

; collisions: https://stackoverflow.com/a/19614185

(defn wait [ms]
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

(defn set-fn
  ([entity props]
    (let [tmp (h "img" (j/lit {:style (get-style props)}))]
      (j/assoc! entity :props props)
      ; TODO: is there a faster way to copy styles than text conversion?
      ; https://github.com/hyperhype/hyperscript/blob/master/index.js#L82-L98
      (j/assoc-in! entity [:element :style] (j/get-in tmp [:style :cssText]))))
  ([entity k v]
    (let [props (j/get entity :props)]
      (j/assoc! props k v)
      (set-fn entity props))))

(defn image [url & [props]]
  (p/let [i (load-image url)]
    (let [style (when props (get-style props))
          el (h "img" (j/lit {:src (j/get i :src)
                              :className "twge-entity"
                              :style style}))
          entity (j/lit {:element el})]
      (j/assoc! entity :set (partial set-fn entity))
      (j/assoc! entity :get #(j/get-in entity [:props %])))))

(defn emoji [character & [props]]
  (let [code-point (j/call character :codePointAt 0)
        hex (j/call code-point :toString 16)
        url (str "https://raw.githubusercontent.com/twitter/twemoji/master/assets/svg/" hex ".svg")]
    (image url props)))

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

(defn frame []
  (js/Promise.
    (fn [res]
      (let [now (js/Date.)]
        (js/requestAnimationFrame #(res (- (js/Date.) now)))))))
