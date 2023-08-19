import { emoji, container, scene, frame, happened, redraw } from "./twge.js";
// or "https://cdn.jsdelivr.net/gh/chr15m/tiny-web-game-engine/twge.js"

var ghost = await emoji("👻", {"w": 1, "h": 1});
var tree = await emoji("🌲", {"w": 1, "h": 1, "x": -2});

var s = scene();
s.add(tree);
s.add(ghost);

while (true) {
  var [ elapsed, events ] = await frame();
  if (events.keyheld.ArrowRight) {
    ghost.x += 0.1;
  }
  if (events.keyheld.ArrowLeft) {
    ghost.x -= 0.1;
  }
  if (events.keyheld.ArrowUp) {
    ghost.y += 0.1;
  }
  if (events.keyheld.ArrowDown) {
    ghost.y -= 0.1;
  }
  redraw(s);
}
