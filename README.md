Minimal 2d game engine which runs in your browser.

**This is a WIP and not ready for general use.**

See the [API documentation](./API.md) for details about each function of the engine.

```javascript
import { emoji, image, scene, redraw, frame, happened } from "https://cdn.jsdelivr.net/gh/chr15m/tiny-web-game-engine/twge.js"
```

```html
<link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/chr15m/tiny-web-game-engine/twge.css"/>
```

```javascript
import { emoji, scene, frame, happened } from "https://cdn.jsdelivr.net/gh/chr15m/tiny-web-game-engine/twge.js"

var ghost = await emoji("👻", {"w": 64, "h": 64, "vy": 0});

var s = scene();
s.add(ghost);

while (true) {
  var [ elapsed, events ] = await frame();
  if (happened(events, "ArrowRight")) {
    ghost.x += 64;
  }
  if (happened(events, "ArrowLeft")) {
    ghost.x -= 64;
  }
  redraw(s);
}
```

