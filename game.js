import { timeout } from "./twge.js";

console.log("before");
var t = await timeout(1000);
console.log("after");

/*
await dino = image("dino.png")
await cactus = image("cactus.png")

height = 0;
v = 0;
gravity = 2;

forever(() => {
  if (keyDown(13)) {
    v = 5;
  }
  // keyPressed(13)

  draw(dino, 0, 0)
  draw(cactus, 10, 0)
}, 42);
*/
