(ns twge
  "This documentation describes the different functions you can use to make a game.

  ## Notes

  - Entities at 0,0 appear in the center of the screen."
  (:require
    [promesa.core :as p]
    [applied-science.js-interop :as j]
    ["hyperscript" :as h]))

; the weird clojurescript coding style in this file is to retain a small build size

; TODO: effect function to apply css effects
; TODO: check held keys
; TODO: catch browser errors and show a popup about opening the console
; TODO: throw kid-friendly error messages for things like missing args
; TODO: function to get scene size
; TODO: transformed collisions: https://stackoverflow.com/a/19614185

(def unit "vmin")
(def scale 8)

(defn sleep
  "Returns a promise (so use `await`) which finishes after `ms` delay."
  [ms]
  (p/delay ms))

(def all
  "Wait for an array of several awaits."
  (js/Promise.all.bind js/Promise))

; functional js-delete
(defn del [s k]
  (when s
    (js-delete s k))
  s)

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
          (aset style (.concat "--" k) (.toString (default-unit k v))))
        style))
    #js {}))

(defn assign
  "Set properties of an entity.
  You can pass the property name and the value such as `x` and `12`,
  or a set of key-value pairs to set more than one like this: `{x: 23, y: 15}`."
  [entity k-or-props v]
  (if v
    (aset entity k-or-props v)
    (js/Object.assign entity k-or-props)))

; *** drawing routines *** ;

(def style-holder (h "img"))

(defn style-to-string [styles]
  (aset style-holder "style" "")
  (-> styles
      js/Object.keys
      (.forEach #(-> style-holder .-style (.setProperty % (aget styles %)))))
  (aget style-holder "style" "cssText"))

(defn recompute-styles [ent]
  (let [element (j/get ent :element)
        style (get-style ent)
        style-string (style-to-string style)
        old-style (j/get element :twge-style)]
    (when (not (coercive-= old-style style-string))
      (aset element "style" style-string)
      (aset element "twge-style" style-string)))
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
      (.forEach (js/Array.from (aget ent "element" "children"))
                #(redraw (aget % "entity")))))
  ent)

(defn add
  "Add an entity to a parent.
  
  - `parent` is usually the scene or a container entity."
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
  Emoji entities will always be square. Set their size using the width (w) setting.
  
  - `character` is the literal emoji character such as '👻'."
  [character props]
  (let [code-points (.map (js/Array.from character) #(j/call % :codePointAt 0))
        hexes (.map code-points #(j/call % :toString 16))
        url (.concat "https://raw.githubusercontent.com/"
                     "twitter/twemoji/master/assets/svg/"
                     (.join hexes "-") ".svg")]
    (image url (assign #js {:w 1 :h 1} props nil))))

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
      (.forEach children (fn [c] (add e c))))
    (recompute-styles e)
    e))

; *** event handling functions *** ;

(defn cycle-events [events]
  (j/lit
    {:last (js/Object.assign #js {} (del events "last"))
     :keydown {}
     :keyup {}
     :keyheld (if events (aget events "keyheld") #js {})}))

; TODO: move events global onto scene?
(def events (cycle-events nil))

(defn add-key-event [events ev]
  (let [code (j/get ev :code)
        kind (j/get ev :type)
        held? (-> events
                  (j/get :last) (or #js {})
                  (j/get :keyheld) (or #js {})
                  (aget code))
        keyheld (j/get events :keyheld)]
    (cond (coercive-= kind "keyup")
          (do
            (del keyheld code)
            (aset events kind code ev)
            events)
          (and (coercive-= kind "keydown") (not held?))
          (do
            (aset events kind code ev)
            (aset events "keyheld" code ev)
            events)
          :else events)))

; *** scene related functions *** ;

(defn scene
  "Create a new scene data structure.
  
  - `props` is an optional object to set the scene properties. Here are some fields:
    - `element` - HTML element to use other than `#twge-default`.
    - `scale` - how much to scale the game by."
  [props]
  (let [s (assign #js {:element (.getElementById js/document "twge-default")
                       :scale scale
                       :unit "vmin"} ; unit not actually used yet
                  props nil)]
    (j/assoc! (j/get s :element) :innerHTML "")
    (.addEventListener js/document "keydown" #(add-key-event events %))
    (.addEventListener js/document "keyup" #(add-key-event events %))
    (j/assoc! s :add #(add s %))))

(defn frame
  "Wait for the next animation frame.
  Generally you should call this once per game loop with `await` as it returns a Promise.

  - returns a Promise holding [elapsed-time, events]
  - `elapsed-time` is the number of milliseconds since the last frame.
  - `events` is an object holding input events that occured since the last frame.
  
  The `events` key has subobjects that can be used like this:
  - `events.keydown.ArrowUp` - check if arrow up key was pressed during this frame.
  - `events.keyheld.KeyA` - check if the 'A' key is held.
  - `events.keyup.Escape` - check if the `Escape` key was released during this frame."
  []
  (js/Promise.
    (fn [res]
      (let [now (js/Date.)]
        (js/requestAnimationFrame
          #(let [queued-events (js/Object.assign #js {} events)]
             (set! events (cycle-events events))
             (res (j/lit [(- (js/Date.) now) queued-events]))))))))

(defn bbox
  "Get the bounding box of the entity's element.

  The box returned has these properties (in pixels):
  `x`, `y`, `width`, `height`, `top`, `right`, `bottom`, `left`."
  [entity]
  (j/call (j/get entity :element) :getBoundingClientRect))

(defn collided
  "Check to see if an entity has collided with a list of other entities.

  - `entity` is the entity you want to check for collisions.
  - `entities` is the array of other entities you want to check for collisions with `entity`.

  Returns an array of entities that collided."
  [entity entities overlap]
  (let [target-bbox (bbox entity)
        overlap (or overlap 0)]
    (.filter entities
             (fn [other-entity]
               (when (not (coercive-= entity other-entity))
                 (let [other-bbox (bbox other-entity)]
                   (and (< (+ (j/get target-bbox :left) overlap) (j/get other-bbox :right))
                        (> (- (j/get target-bbox :right) overlap) (j/get other-bbox :left))
                        (< (+ (j/get target-bbox :top) overlap) (j/get other-bbox :bottom))
                        (> (- (j/get target-bbox :bottom) overlap) (j/get other-bbox :top)))))))))
