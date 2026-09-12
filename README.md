# SkyLimitless

Fabric mod for Minecraft 1.20.1 that raises the Overworld build-height limit while keeping the vanilla bottom Y coordinate at -64.

## Default

The default requested top Y is `500`.

Minecraft chunk sections are 16 blocks tall, so the requested value is rounded upward to the next section boundary. Therefore the default effective top Y is `512`, which means the highest placeable block is `Y=511`.

Existing terrain coordinates are not moved because the bottom remains `Y=-64`.

## Commands

Commands require operator permission level 2.

- `/skylimitless status` — shows the requested and effective height.
- `/skylimitless height <top_y>` — saves a new requested top Y.

After changing the value with the command, **restart the server/world**. The mod intentionally does not change the height of an already-running world because active chunk sections depend on the dimension's height.

Example:

```text
/skylimitless height 800
```

The setting is also stored in:

```text
config/skylimitless.properties
```

The current setting is loaded before the world dimension is constructed, and the Overworld `DimensionType` is adjusted there. This keeps server-side and client-side dimension data consistent instead of changing only individual world/chunk methods.

## Important compatibility behavior

SkyLimitless only raises the vertical build volume. It does not rewrite the world seed, terrain generator, biome generation, or the vanilla bottom Y coordinate.

The extra space above the vanilla terrain height is therefore available for building without changing existing terrain below it.

## Build environment

- Minecraft 1.20.1
- Fabric Loader 0.15.11
- Fabric API 0.92.11+1.20.1
- Fabric Loom 1.9.2
- Gradle 8.12
- Java 17

GitHub Actions builds the mod automatically on pushes and pull requests targeting `minecraft-1.20.1-fabric`.
