import { emoji, image, scene, frame, happened } from "./twge.js";
// import {wait, image, emoji, scene, frame, happened } from "https://cdn.jsdelivr.net/gh/chr15m/tiny-web-game-engine/twge.js"

//var face = await image("https://raw.githubusercontent.com/twitter/twemoji/master/assets/svg/1f603.svg")
//var face = await image("https://cdn2.f-cdn.com/contestentries/1093125/13547116/5987856c88b79_thumb900.jpg")
//var bomb = await image("https://raw.githubusercontent.com/twitter/twemoji/master/assets/svg/1f4a3.svg")

var face = await emoji("👻");
var square = await emoji("🟧");

//face.set({"x": 300, "y": -50, "w": 64, "h": 64});
face.set({"w": 64, "h": 64});
square.set({"w": 64, "h": 64});

var s = scene();
s.add(square);
s.add(face);

while (true) {
  var [ elapsed, events ] = await frame();
  if (happened(events, "ArrowRight")) {
    face.set("x", face.get("x") + 64);
  }
  if (happened(events, "ArrowLeft")) {
    face.set("x", face.get("x") + -64);
  }
  //redraw([face]);
}
