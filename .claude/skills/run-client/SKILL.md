---
name: run-client
description: Launch the Minecraft client for manual/visual verification of this mod
---

Launch the game client to visually verify a change (item textures, GUI, rendering, etc.):

1. Change directory to repo root
2. Run `./gradlew runClient`

This opens a full Minecraft client window (requires a display/GPU) and takes a while to boot
(asset/model loading, mod init). There is no scripted input driver for this project — verification
means having a human look at the running game (or explicitly asking the user to check), not
`grep`-ing logs or diffing generated JSON alone.
