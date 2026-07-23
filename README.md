# Spellgems

Minecraft **26.1.2** mod: spell gems, wands, mana machines, and enchanting.

**Loaders:** Fabric and NeoForge (separate jars — not interchangeable).

| | Fabric | NeoForge |
|---|---|---|
| API | Fabric API `0.145.4+26.1.2` | NeoForge `26.1.2.84` |
| Module | `:fabric` | `:neoforge` |
| Install jar | `spellgems-<version>-fabric.jar` | `spellgems-<version>-neoforge.jar` |

## Project layout

```
common/     # shared gameplay, assets, generated data (no loader APIs)
fabric/     # Fabric Loom entrypoints + platform.fabric
neoforge/   # ModDevGradle entrypoints + platform.neoforge
```

## Build / run / release

### Development runs

```bash
# Fabric
./gradlew :fabric:runClient
./gradlew :fabric:runServer

# NeoForge
./gradlew :neoforge:runClient
./gradlew :neoforge:runServer
```

Run directories (keep separate):

- Fabric: `fabric/run/`
- NeoForge: `neoforge/run/` (MDG default)

Dedicated server: accept `eula.txt`, set `online-mode=false` for offline dev.

### Datagen (Fabric only)

```bash
./gradlew :fabric:runDatagen
```

Output: `common/src/main/generated` (packaged into **both** loader jars).

### Production jars

```bash
# Build both loaders and collect installable jars
./gradlew releaseJars
```

Artifacts:

```
build/release/spellgems-1.0.0-fabric.jar
build/release/spellgems-1.0.0-neoforge.jar
```

Or build one loader:

```bash
./gradlew :fabric:build    # → fabric/build/libs/spellgems-*-fabric.jar
./gradlew :neoforge:build  # → neoforge/build/libs/spellgems-*-neoforge.jar
```

### Publishing (Modrinth / CurseForge / Maven)

- Upload **two files** per version, marked for the correct loader + MC `26.1.2`.
- Same mod version string is fine; file names distinguish loaders.
- Do **not** ship a single “universal” jar.
- JEI is optional at runtime (dev runs pull JEI via Gradle; players install JEI separately).

Maven artifactIds (if publishing to a Maven repo):

- `spellgems-fabric`
- `spellgems-neoforge`

## Requirements

- JDK **25**
- Gradle wrapper (included)

## Verification

See [docs/multi-loader-verification.md](docs/multi-loader-verification.md) for the dual-loader checklist (WS5).

## License

CC0 (see `LICENSE`). Feel free to learn from it and incorporate it in your own projects.
