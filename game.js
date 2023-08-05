import { timeout, image, scene } from "./twge.js";

var face = await image("https://raw.githubusercontent.com/twitter/twemoji/master/assets/svg/1f603.svg")
var bomb = await image("https://raw.githubusercontent.com/twitter/twemoji/master/assets/svg/1f4a3.svg")

/* draw([face({"x": "300px", "y": "-50px", "w": "64px", "h": "64px"}),
  face({"x": "50px", "y": "200px", "w": "64px", "h": "64px"}),
  bomb({"x": "-200px", "w": "64px", "h": "64px"})])*/

//await timeout(500);

face.set({"x": "300px", "y": "-50px", "w": "64px", "h": "64px"});

var s = scene.new();
//await timeout(1);
scene.add(s, face);

// console.log(goober());

// scene_add(face);

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
