# common

Loader-agnostic Spellgems sources and resources.

- **Compiled by** `:fabric` and (later) `:neoforge` via Gradle source-set inclusion — this module does not produce a Minecraft-aware JAR by itself.
- **Must not** import `net.fabricmc.*` or `net.neoforged.*`.
- Platform interfaces: `net.pnovaczek.spellgems.platform` / `platform.client`
- Content registration: `ModRegistries.registerAll()`
