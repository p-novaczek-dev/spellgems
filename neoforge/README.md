# neoforge

NeoForge loader module (ModDevGradle 2.0.142, NeoForge `26.1.2.84`).

```bash
./gradlew :neoforge:runClient
./gradlew :neoforge:runServer
./gradlew :neoforge:build
# jar → neoforge/build/libs/spellgems-<version>-neoforge.jar

# Both loaders into build/release/
./gradlew releaseJars
```

Common gameplay is source-included from `../common`. Platform impls live under `platform.neoforge` / `platform.client.neoforge`.

**JEI:** optional via `localRuntime` NeoForge JEI artifact. Custom recipes sync with `OnDatapackSyncEvent` + `RecipesReceivedEvent`.

**Datagen:** use Fabric (`./gradlew :fabric:runDatagen`); generated JSON under `common/src/main/generated` is already on this module’s resource path.
