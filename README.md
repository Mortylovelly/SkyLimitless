# SkyLimitless — NeoForge 1.21.1

NeoForge version of SkyLimitless for Minecraft 1.21.1.

## What it does

SkyLimitless raises the Overworld build-height limit while keeping the vanilla bottom Y coordinate at `-64`. The default requested top is `500`, which is rounded upward to the nearest 16-block section boundary, so the default effective top is `512` and the highest placeable block is `Y=511`.

Existing terrain coordinates are not moved.

## Commands

Requires permission level 2:

```text
/skylimitless status
/skylimitless height 800
```

The height setting is saved to `config/skylimitless.properties` and becomes active after restarting the world/server. The mod intentionally does not resize an already-running world because changing the dimension height live can invalidate active chunk sections.

## NeoForge version

- Minecraft 1.21.1
- NeoForge 21.1.235
- Java 21
- NeoGradle 7.1.38
- Branch: `neoforge-1.21.1`

The Fabric implementation remains on `main` and is not modified by this branch.
