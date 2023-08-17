##  sleep  ( ms )

Returns a promise (so use `await`) which finishes after `ms` delay.

##  all

Wait for an array of several awaits.

##  assign  ( entity k-or-props v )

Set properties of an entity.
  You can pass the property name and the value such as `x` and `12`,
  or a set of key-value pairs to set more than one like this: `{x: 23, y: 15}`.

##  redraw  ( ent )

Redraw a scene or entity. If a scene is passed it recursively redraws all entities.
  Usually you should call this once on the scene at the end of each game loop.
  If you have changed an entity's properties like x, y position,
  calling this will update the entity on the screen to the new position.

##  add  ( parent entity )

Add an entity to a parent.
  
  - `parent` is usually going to be the scene.

##  entity  ( props )

Create a new entity data structure.

  - `props` are optional initial properties to set such as `x`, `y`, `w`, `h`, etc.

##  image  ( url props )

Create a new `entity` data structure based on a single image.

##  emoji  ( character props )

Create a new `entity` data structure based on an emoji.
  Emoji entities will always be square. Set their size using the width (w) setting.
  
  - `character` is the literal emoji character such as '👻'.

##  container  ( props children )

Create a new `entity` data structure that acts as a container for other entities.
  
  The container can hold multiple entities and can be added to a parent (like a scene or another container) as a single entity.

##  scene  ( props )

Create a new scene data structure.
  
  - `props` is an optional object to set the scene properties. Here are some fields:
    - `element` - HTML element to use other than `#twge-default`.
    - `scale` - how much to scale the game by.

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

