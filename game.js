import { emoji, scene, frame, happened } from "./twge.js";
// import {wait, image, emoji, scene, frame, happened } from "https://cdn.jsdelivr.net/gh/chr15m/tiny-web-game-engine/twge.js"

//var face = await image("https://raw.githubusercontent.com/twitter/twemoji/master/assets/svg/1f603.svg")
//var face = await image("https://cdn2.f-cdn.com/contestentries/1093125/13547116/5987856c88b79_thumb900.jpg")
//var bomb = await image("https://raw.githubusercontent.com/twitter/twemoji/master/assets/svg/1f4a3.svg")

var face = await emoji("👻");

face.set({"x": 300, "y": -50, "w": 64, "h": 64});

var s = scene();
s.add(face);

face.set("x", -200);

console.log("x", face.get("x"));

while (true) {
  var [ elapsed, events ] = await frame();
  if (happened(events, "ArrowRight")) {
    var x = face.get("x");
    face.set("x", x + 100);
  }
  if (happened(events, "ArrowLeft")) {
    var x = face.get("x");
    face.set("x", x + -100);
  }
  //redraw([face]);
}
