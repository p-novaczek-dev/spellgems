# common

Loader-agnostic Spellgems sources and resources.

- **Compiled by** `:fabric` and `:neoforge` via Gradle source-set inclusion — this module does not produce a Minecraft-aware JAR by itself.
- **Must not** import `net.fabricmc.*` or `net.neoforged.*`.
- Platform interfaces: `net.pnovaczek.spellgems.platform` / `platform.client`
- Content registration: `ModRegistries.registerAll()` / `registerFor` (Neo `RegisterEvent`)
- **Datagen:** run on Fabric only (`./gradlew :fabric:runDatagen`); output is `src/main/generated` here and is packaged into both jars
- **JEI:** shared plugin under `client/jei` (`@JeiPlugin`); recipe sync via `PlatformClient`
