##  sleep  ( ms )

Returns a promise (so use `await`) which finishes after `ms` delay.

##  assign  ( entity k-or-props v )

Set properties of an entity.
  You can pass the property name and the value such as `x` and `12`,
  or a set of key-value pairs to set more than one like this: `{x: 23, y: 15}`.

##  redraw  ( ent )

Redraw a scene or entity. If a scene is passed it recursively redraws all entities.
  Usually you should call this once on the scene at the end of each game loop.
  If you have changed an entity's properties like x, y position,
  calling this will update the entity on the screen to the new position.

##  entity  ( props )

Create a new entity data structure.
  
  - `props` are optional initial properties to set such as `x`, `y`, `w`, `h`, etc.

##  image  ( url props )

Create a new `entity` data structure based on a single image.

##  emoji  ( character props )

Create a new `entity` data structure based on an emoji.
  
  - `character` is the literal emoji character such as '👻'.

##  add  ( parent entity )

Add an entity to a parent.
  
  - `parent` is usually going to be the scene.

##  scene  ( element )

Create a new scene data structure.
  
  - `element` is an optional argument to set up the scene in an
    HTML element other than `twge-default`.

##  frame  (  )

Wait for the next animation frame.
  Generally you should call this once per game loop with `await` as it returns a Promise.

  - returns a Promise holding [elapsed-time, events]
  - `elapsed-time` is the number of milliseconds since the last frame.
  - `events` is a list of input events that occured since the last frame.

##  happened  ( events code event-type )

Test if specific events happened in an event list (such as `events` passed back from the `frame` call).
  
  - `events` is a list of events to pass in. Usually from the `frame` call.
  - `code` is the key-code to check on keydown events.
  - `event-type` is optional and is an event type like `keydown` or `keyup`.
