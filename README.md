🥚 Minimal 2d browser game engine.

`ege` is for ~~economical~~ ~~emoji~~ ~~efficient~~ ~~eensy~~ egg game engine.

**This is a WIP and not ready for general use.**

See the [API documentation](./API.md) for details about each function of the engine.

Download [the template project]() to get started!

## Use it

```html
<link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/chr15m/tiny-web-game-engine/twge.css"/>
```

```html
<script type="module">
  import { emoji, scene, frame, happened } from "https://cdn.jsdelivr.net/gh/chr15m/tiny-web-game-engine/twge.js"

  var ghost = await emoji("👻", {"w": 1, "h": 1, "vy": 0});

  var s = scene();
  s.add(ghost);

  while (true) {
    var [ elapsed, events ] = await frame();
    if (events.keyheld.ArrowRight) {
      ghost.x += 1;
    }
    if (events.keyheld.ArrowLeft) {
      ghost.x -= 1;
    }
    redraw(s);
  }
</script>
```

