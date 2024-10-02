# CloudProtections

Minecraft paper plugin for protecting spherical/cubic regions from mobs/players. Look at the `/cprots` command ingame
for how to use this plugin.

## Download

https://dl.cloudcraftmc.de/cloudprotections

**Note: Depends on [CommandAPI](https://commandapi.jorel.dev/)
and [CloudCore](https://github.com/CloudCraftProjects/CloudCore/).**

## Usage

- Use `/cprots list` to list all protection regions
- Use `/cprots create <box|sphere> <id> <pos1> <pos2|radius> [<dimension>]` to create a cubic or spherical region
    - A created region is protected from all flags by default
- Use `/cprots modify <region> delete` to delete a specific region
- Use `/cprots modify <region> rename <id>` to rename a specific region
- Use `/cprots modify <region> flags <add|remove> <flag1> [<flag2>] [<flag3>] ...` to add or remove specific flags from a specific region
- Use `/cprots modify <region> flags list` to list the flags currently active for the specific region
- Use `/cprots modify <region> exclusions <add|remove> <uuid>` to add or remove specific exclusions from a specific region
    - The specified UUID may be the UUID of an entity which is ignored by this region
- Use `/cprots modify <region> exclusions list` to list the exclusions currently active for the specific region
- Use `/cprots modify <region> priority <priority>` to change the priority of regions
    - This is useful for nesting multiple regions
    - The higher the priority, the more the specific region has to say about wether a specific interaction is allowed or not

<details>
<summary><strong>Using this as API</strong></summary>

### Dependency

Add the following to your `build.gradle.kts`:

```kotlin
repositories {
    maven("https://repo.cloudcraftmc.de/releases/")
}

dependencies {
    compileOnly("dev.booky:cloudprotections:{VERSION}")
}
```

### Usage

You can get the `ProtectionsManager` instance using bukkit's `ServicesManager`. For updating regions
use `ProtectionsManager#updateRegions`. This instantly saves the updated regions to file. Creating a new region
can be done using `new ProtectionRegion`. Exclusions and region flags are modifiable, the area, id and priority is not
modifiable.

</details>

## License

Licensed under GPL-3.0, see [LICENSE](./LICENSE) for further information.
