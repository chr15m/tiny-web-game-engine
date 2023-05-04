import { timeout, draw, image } from "./twge.js";

var face = await image("https://raw.githubusercontent.com/twitter/twemoji/master/assets/svg/1f603.svg")
var bomb = await image("https://raw.githubusercontent.com/twitter/twemoji/master/assets/svg/1f4a3.svg")

draw([face({"x": "300px", "y": "-50px"}), face({"x": "50px", "y": "200px"}), bomb({"x": "-200px"})])

/*
forever(() => {
  if (keyDown(13)) {
    v = 5;
  }
  // keyPressed(13)

  draw(dino, 0, 0)
  draw(cactus, 10, 0)
}, 42);
*/
