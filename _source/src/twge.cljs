(ns twge
  "This documentation describes the different functions you can use to make a game.

  Some notes about this game engine:

  - Entities at 0,0 appear in the center of the screen."
  (:require
    [promesa.core :as p]
    [applied-science.js-interop :as j]
    ["hyperscript" :as h]))

; the weird clojurescript coding style in this file is to retain a small build size

; TODO: collisions: https://stackoverflow.com/a/19614185
; TODO: catch browser errors and show a popup about opening the console
; TODO: throw kid-friendly error messages for things like missing args
; TODO: function to get scene size

(def unit "vmin")

(defn sleep
  "Returns a promise (so use `await`) which finishes after `ms` delay."
  [ms]
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
  (let [unit (when (not (coercive-= (.indexOf (j/lit [:x :y :w :h]) t) -1)) unit)]
    (if (and unit
             (or (coercive-= (type v) js/Number)
                 (coercive-= (.toString (js/parseFloat v)) v)))
      (.concat "" v unit)
      v)))

(defn get-style [props]
  (.reduce
    #js ["x" "y" "w" "h" "scale"]
    (fn [style k]
      (let [v (aget props k)]
        (when v
          (aset style (.concat "--" k) (str (default-unit k v))))
        style))
    #js {}))

(defn assign
  "Set properties of an entity.
  You can pass the property name and the value such as `x` and `12`,
  or a set of key-value pairs to set more than one like this: `{x: 23, y: 15}`."
  [entity k-or-props v]
  (if v
    (assign entity (j/lit {k-or-props v}) nil)
    (js/Object.assign entity k-or-props)))

; *** drawing routines *** ;

(defn recompute-styles [ent]
  (let [tmp (h "img" (j/lit {:style (get-style ent)}))
        style-string (-> (j/get tmp :style) (j/get :cssText))
        old-style (-> ent (j/get :element) (j/get :style) (j/get :cssText))]
    ; TODO: is there a faster way to copy styles than text conversion?
    ; https://github.com/hyperhype/hyperscript/blob/master/index.js#L82-L98
    (when (not (coercive-= old-style style-string))
      (aset ent "element" "style" style-string)))
  ent)

(defn redraw
  "Redraw a scene or entity. If a scene is passed it recursively redraws all entities.
  Usually you should call this once on the scene at the end of each game loop.
  If you have changed an entity's properties like x, y position,
  calling this will update the entity on the screen to the new position."
  [ent]
  (when-let [el (aget ent "element")]
    ; (js/console.log (j/call-in el #js [:classList :contains] "hello"))
    (when (or (-> el .-classList (.contains "twge-entity"))
              (-> el .-classList (.contains "twge")))
      (recompute-styles ent)
      (.map (js/Array.from (aget ent "element" "children"))
            #(redraw (aget % "entity")))))
  ent)

(defn add
  "Add an entity to a parent.
  
  - `parent` is usually going to be the scene."
  [parent entity]
  (j/call (j/get parent :element)
          :appendChild
          (j/get entity :element)))

; *** entity types *** ;

(defn entity
  "Create a new entity data structure.

  - `props` are optional initial properties to set such as `x`, `y`, `w`, `h`, etc."
  [props]
  (assign
    #js {:x 0 :y 0}
    props nil))

(defn image
  "Create a new `entity` data structure based on a single image."
  [url props]
  (-> (load-image url)
      (.then
        (fn [i]
          (let [e (entity props)
                style (get-style e)
                el (h "img" (j/lit {:src (j/get i :src)
                                    :className "twge twge-entity"
                                    :style style
                                    :entity e}))]
            (j/assoc! e :element el)
            (j/assoc! e :assign (.bind assign nil e))
            (recompute-styles e)
            e)))))

(defn emoji
  "Create a new `entity` data structure based on an emoji.
  
  - `character` is the literal emoji character such as '👻'."
  [character props]
  (let [code-points (.map (js/Array.from character) #(j/call % :codePointAt 0))
        hexes (.map code-points #(j/call % :toString 16))
        url (.concat "https://raw.githubusercontent.com/"
                     "twitter/twemoji/master/assets/svg/"
                     (.join hexes "-") ".svg")]
    (image url props)))

(defn container
  "Create a new `entity` data structure that acts as a container for other entities.
  
  The container can hold multiple entities and can be added to a parent (like a scene or another container) as a single entity."
  [props children]
  (let [e (entity props)
        style (get-style e)
        el (h "div" (j/lit {:className "twge twge-entity twge-container"
                            :style style
                            :entity e}))]
    (j/assoc! e :element el)
    (j/assoc! e :assign (.bind assign nil e))
    (j/assoc! e :add #(add e %))
    (when children
      (.map children (fn [c] (add e c))))
    (recompute-styles e)
    e))

; *** scene related functions *** ;

; TODO: move this global onto scene?
(def events #js [])

(defn scene
  "Create a new scene data structure.
  
  - `props` is an optional object to set the scene properties. Here are some fields:
    - `element` - HTML element to use other than `#twge-default`.
    - `scale` - how much to scale the game by.
    - `unit` - the default unit if unspecified (default is 'vim' which divides the screen up into 100 tiles)."
  [props]
  (let [s (assign #js {:element (.getElementById js/document "twge-default")
                       :scale 4
                       :unit "vim"}
                  props nil)]
    (j/assoc! (j/get s :element) :innerHTML "")
    ;(recompute-styles s)
    (.addEventListener js/document "keydown" #(.push events %))
    (j/assoc! s :add #(add s %))))

(defn frame
  "Wait for the next animation frame.
  Generally you should call this once per game loop with `await` as it returns a Promise.

  - returns a Promise holding [elapsed-time, events]
  - `elapsed-time` is the number of milliseconds since the last frame.
  - `events` is a list of input events that occured since the last frame."
  []
  (js/Promise.
    (fn [res]
      (let [now (js/Date.)]
        (js/requestAnimationFrame
          #(let [queued-events (.splice events 0 (j/get events :length))]
             (res (j/lit [(- (js/Date.) now) queued-events]))))))))

(defn happened
  "Test if specific events happened in an event list (such as `events` passed back from the `frame` call).
  
  - `events` is a list of events to pass in. Usually from the `frame` call.
  - `code` is the key-code to check on keydown events.
  - `event-type` is optional and is an event type like `keydown` or `keyup`."
  [events code event-type]
  (let [found (.filter events #(coercive-= (j/get % :code) code))
        found (if event-type
                (.filter found #(coercive-= (j/get % :type) event-type))
                found)]
    (not (coercive-= (j/get found :length) 0))))
