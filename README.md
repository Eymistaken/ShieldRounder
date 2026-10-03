# Eymistaken's Shield Rounder

ShieldRounder is a client-side Fabric mod that visualizes the protective hemisphere of actively raised shields, with wireframe and solid render modes and a Mod Menu configuration screen.

Version 1.0.3 uses the same JAR for Minecraft 26.1, 26.1.1, 26.1.2, 26.2, and 26.3. Install Java 25, Fabric Loader 0.19.3 or newer, and Fabric API and Mod Menu releases matching your Minecraft version. Fabric Loader 0.19.5 is used for the current development build.

## Compatibility

These dependency combinations were used to verify the same production classes compiled for Minecraft 26.3:

| Minecraft | Fabric API | Mod Menu | Development runtime Placeholder API |
| --- | --- | --- | --- |
| 26.1 | 0.145.1+26.1 | 18.0.2 | 3.0.0+26.1 |
| 26.1.1 | 0.145.4+26.1.1 | 18.0.2 | 3.0.0+26.1 |
| 26.1.2 | 0.155.3+26.1.2 | 18.0.2 | 3.0.0+26.1 |
| 26.2 | 0.153.0+26.2 | 20.0.0-alpha.1 | 3.1.0-beta.1+26.2 |
| 26.3 | 0.161.0+26.3 | 21.0.0 | 3.2.0+26.3 |

Screen ownership moved from `Minecraft` to `Gui` in 26.2. The configuration screen resolves and caches the appropriate `setScreen` method once, so closing it returns to its parent on every supported version. Rendering uses the camera position provided by Fabric's level render state, avoiding the camera accessor that was renamed between versions. Frames without an initialized camera are skipped.

Minecraft 1.21.x and older require a separate backport because their renderer and GUI APIs differ. Snapshots and future Minecraft releases are not included in the compatibility declaration.

## Build and verification

Use Java 25:

```sh
./gradlew test
./gradlew build
./gradlew clean build --refresh-dependencies --stacktrace
```

The distributable is `build/libs/shield-rounder-1.0.3.jar`. Dependencies are not bundled; use releases matching the game version.

To test the current 26.3 production bytecode with the 26.2 dependencies, first run the default build, then run:

```sh
./gradlew test --rerun-tasks -x compileJava \
  -Pminecraft_version=26.2 \
  -Ploader_version=0.19.3 \
  -Pfabric_api_version=0.153.0+26.2 \
  -Pmodmenu_version=20.0.0-alpha.1 \
  -Pplaceholder_api_version=3.1.0-beta.1+26.2
```

Use the corresponding dependency versions from the table for the other compatibility checks. Keep `-x compileJava` so these checks exercise the release bytecode rather than recompiling it for each older game. Run the default build again afterward to restore the 26.3 development environment.

Automated verification covers geometry, colors, flash, deployment, and glint behavior. Release verification also checked the compiled classes' referenced API members and resolved the configuration screen setter against all five runtimes. Interactive in-game visual testing was not performed.
