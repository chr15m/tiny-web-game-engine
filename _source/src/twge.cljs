(ns twge
  (:require
    [promesa.core :as p]
    [applied-science.js-interop :as j]
    ["hyperscript" :as h]))

; the weird clojurescript coding style in this file is to retain a small build size

; TODO: collisions: https://stackoverflow.com/a/19614185
; TODO: catch browser errors and show a popup about opening the console

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
  (let [unit (when (not (coercive-= (.indexOf (j/lit [:x :y :w :h]) t) -1)) "px")]
    (if (or (coercive-= (type v) js/Number)
            (coercive-= (.toString (js/parseFloat v)) v))
      (.concat "" v unit)
      v)))

(defn get-style [props]
  (.reduce
    #js ["x" "y" "w" "h"]
    (fn [style k]
      (let [v (aget props k)]
        (when v
          (aset style (.concat "--" k) (default-unit k v)))
        style))
    #js {}))

(defn set-fn
  [entity k-or-props v]
  (if v
    (let [props (aget entity "props")]
      (aset props k-or-props v)
      (set-fn entity props nil))
    (let [tmp (h "img" (j/lit {:style (get-style k-or-props)}))
          style-string (-> (j/get tmp :style) (j/get :cssText))]
      (j/assoc! entity :props k-or-props)
      ; TODO: is there a faster way to copy styles than text conversion?
      ; https://github.com/hyperhype/hyperscript/blob/master/index.js#L82-L98
      (aset entity "element" "style" style-string))))

(defn image [url props]
  (-> (load-image url)
      (.then
        (fn [i]
          (let [style (when props (get-style props))
                el (h "img" (j/lit {:src (j/get i :src)
                                    :className "twge-entity"
                                    :style style}))
                entity (j/lit {:element el})]
            (j/assoc! entity :set (.bind set-fn nil entity))
            (j/assoc! entity :get #(-> (aget entity "props") (aget %)))
            entity)))))

(defn emoji [character props]
  (let [code-point (j/call character :codePointAt 0)
        hex (j/call code-point :toString 16)
        url (.concat "https://raw.githubusercontent.com/twitter/twemoji/master/assets/svg/" hex ".svg")]
    (image url props)))

(def root (.getElementById js/document "twge-default"))
(def events #js [])

(def scene
  (j/lit
    {:new (fn [alternate-root]
            (let [r (or alternate-root root)]
              (j/assoc! r :innerHTML "")
              (.addEventListener js/document "keydown" #(.push events %))
              (.focus r)
              (j/lit {:root r})))
     :add (fn [scene entity]
            (j/call (j/get scene :root)
                    :appendChild
                    (j/get entity :element)))}))

(defn frame []
  (js/Promise.
    (fn [res]
      (let [now (js/Date.)]
        (js/requestAnimationFrame
          #(let [queued-events (.splice events 0 (j/get events :length))]
             (res (j/lit [(- (js/Date.) now) queued-events]))))))))

(defn happened [events code event-type]
  (let [found (.filter events #(coercive-= (j/get % :code) code))
        found (if event-type
                (.filter found #(coercive-= (j/get % :type) event-type))
                found)]
    (not (coercive-= (j/get found :length) 0))))
