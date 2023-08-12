(ns twge
  (:require
    [promesa.core :as p]
    [applied-science.js-interop :as j]
    ["hyperscript" :as h]))

; the weird clojurescript coding style in this file is to retain a small build size

; TODO: collisions: https://stackoverflow.com/a/19614185
; TODO: catch browser errors and show a popup about opening the console
; TODO: throw kid-friendly error messages for things like missing args

(defn wait [ms]
  (p/delay ms))

; *** entity related functions *** ;

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

(defn assign
  [entity k-or-props v]
  (if v
    (let [props (aget entity "props")]
      (aset props k-or-props v)
      (assign entity props nil))
    (js/Object.assign entity k-or-props)))

; *** drawing routines *** ;

(defn recompute-styles [ent]
  (let [tmp (h "img" (j/lit {:style (get-style ent)}))
        style-string (-> (j/get tmp :style) (j/get :cssText))]
    ; TODO: is there a faster way to copy styles than text conversion?
    ; https://github.com/hyperhype/hyperscript/blob/master/index.js#L82-L98
    (aset ent "element" "style" style-string))
  ent)

(defn redraw [ent]
  (when (or (j/call-in ent [:element :classList :contains] "twge-entity")
            (j/call-in ent [:element :classList :contains] "twge"))
    (recompute-styles ent)
    (doseq [el (j/get-in ent [:element :children])]
      (redraw (j/get el :entity))))
  ent)

; *** entity types *** ;

(defn entity [props]
  (j/lit (js/Object.assign
           #js {:x 0 :y 0}
           props)))

(defn image [url props]
  (-> (load-image url)
      (.then
        (fn [i]
          (let [e (entity props)
                style (get-style e)
                el (h "img" (j/lit {:src (j/get i :src)
                                    :className "twge-entity"
                                    :style style
                                    :entity e}))]
            (j/assoc! e :element el)
            (j/assoc! e :assign (.bind assign nil e))
            (recompute-styles e)
            e)))))

(defn emoji [character props]
  (let [code-point (j/call character :codePointAt 0)
        hex (j/call code-point :toString 16)
        url (.concat "https://raw.githubusercontent.com/"
                     "twitter/twemoji/master/assets/svg/"
                     hex ".svg")]
    (image url props)))

; *** scene related functions *** ;

; TODO: move this global onto scene?
(def events #js [])

(defn add [parent entity]
  (j/call (j/get parent :element)
          :appendChild
          (j/get entity :element)))

(defn scene [element]
  (let [r (or element (.getElementById js/document "twge-default"))
        s (j/lit {:element r})]
    (j/assoc! r :innerHTML "")
    (.addEventListener js/document "keydown" #(.push events %))
    (.focus r)
    (j/assoc! s :add #(add s %))))

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
