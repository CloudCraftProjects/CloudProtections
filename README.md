# CloudProtections

Minecraft paper plugin for protecting spherical/cubic regions from mobs/players. Look at the `/cprots` command ingame
for how to use this plugin.

## Download

https://nightly.link/CloudCraftProjects/CloudProtections/workflows/build/master/CloudProtections-Artifacts.zip

**Note: Depends on [CommandAPI](https://commandapi.jorel.dev/)
and [CloudCore](https://github.com/CloudCraftProjects/CloudCore/).**

## Using this as an API

### Dependency

Add the following to your `build.gradle.kts`:

```kotlin
repositories {
    maven("https://repo.cloudcraftmc.de/releases/")
}

dependencies {
    compileOnly("dev.booky:cloudprotections:1.0.4-SNAPSHOT")
}
```

### Usage

You can get the `ProtectionsManager` instance using bukkit's `ServicesManager`. For updating regions
use `ProtectionsManager#updateRegions`. This instantly saves the updated regions to file. Creating a new region
can be done using `new ProtectionRegion`. Exclusions and region flags are modifiable, the area, id and priority is not
modifiable.

## License

Licensed under GPL-3.0, see [LICENSE](./LICENSE) for further information.
