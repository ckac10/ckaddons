# ckaddons DR

Client-side Fabric mod with quality-of-life features for the **Dungeon Realms** server, plus a few vanilla tweaks.

- Minecraft **26.3**, Fabric Loader **0.20.1+**, Java **25**
- Requires **Fabric API**
- Optional: **Mod Menu** (config button) and **YetAnotherConfigLib v3** (full config screen; without it a simpler screen is used)

Open the config with `/ckaddons` or from Mod Menu.

## Features

### Dungeon Realms QoL

**Energy Bar Render**
- Energy bar on crosshair, with an opacity slider
- Hide bar out of combat: the bar only shows after you take damage or hit an entity (1–15 s)

**Render Coords Waypoint**
- Render chests waypoints: when the server says `The key unlocks a nearby vault at X, Y, Z!`, a "Chest" waypoint appears there through walls, colored by distance (0–60 s, 0 = until you change worlds)

**Misc.**
- Rarity overlay behind items, with an opacity slider
- Highlight Beacons: client-side blue glow on beacon buffs (end crystals)
- Tool exp display: level, exp bar and % (or MAX) of your pickaxe / fishing spear next to the selected item name

### Vanilla QoL

**No Render** — hide, one by one: fire overlay, block breaking particles, fog (all kinds), effect display, recipe book, dead entities, death poof particles, vignette

**Screen Title/Subtitle Render**
- Render Title/Subtitle on/off
- Move GUI: drag the title and subtitle to move them, scroll to resize

**Misc.**
- Fullbright
- Compact chat: repeated messages are merged with a counter (time window 0–60 s)
- Selected Item Name Always visible

### Debug
- Copy item NBT (SNBT) to the clipboard with a configurable key: the held item, or the hovered item in an inventory

## Building

```
./gradlew build
```

The mod jar is created in `build/libs/`.

## Credits

Some features were inspired by, or adapted from, these projects:

- [IQ](https://github.com/iqaddons/IQ) (Apache License 2.0): the world rendering of waypoints (box, beam and text through walls) is adapted from its waypoint rendering code.
- [VanillaHUD](https://github.com/Polyfrost/VanillaHUD) (GPL-3.0): idea and mixin approach for moving and scaling the title/subtitle. ckaddons' code was written separately.
- [NoFrills](https://github.com/WhatYouThing/NoFrills): reference for the No Render options.
- [compacting](https://codeberg.org/celestialfault/compacting): reference for Compact chat.
- [Cubes Without Borders](https://github.com/Kira-NT/cubes-without-borders): reference for the config screen layout.
- Based on the [Fabric example mod](https://github.com/FabricMC/fabric-example-mod) template.

## License

ckaddons' own code is released under [CC0 1.0](LICENSE). The code adapted from IQ remains under the Apache License 2.0; see the IQ repository for its license text.
