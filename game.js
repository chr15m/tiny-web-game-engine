import { emoji, image, scene, frame, happened, redraw } from "./twge.js";
// import {wait, image, emoji, scene, frame, happened } from "https://cdn.jsdelivr.net/gh/chr15m/tiny-web-game-engine/twge.js"

var face = await emoji("👻");
var square = await emoji("🟧", {"w": 64, "h": 64});

face.assign({"w": 64, "h": 64});

var s = scene();
s.add(square);
s.add(face);

while (true) {
  var [ elapsed, events ] = await frame();
  if (happened(events, "ArrowRight")) {
    face.x += 64;
  }
  if (happened(events, "ArrowLeft")) {
    face.x -= 64;
  }
  redraw(s);
}
