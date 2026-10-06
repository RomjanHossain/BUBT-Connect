# BUBT Connect

Android app for BUBT, built with Jetpack Compose.

## Tech stack

| Piece | Version |
| --- | --- |
| Gradle plugin (AGP) | 9.4.1 |
| Kotlin | 2.2.10 |
| Compose BOM | 2026.02.01 |
| compileSdk / targetSdk | 37 |
| minSdk | 24 |
| Java | 11 |

Dependencies are versioned centrally in `gradle/libs.versions.toml` via the
Gradle version catalog. Do not hardcode versions in `build.gradle.kts`.

## Getting started

```bash
git clone https://github.com/RomjanHossain/BUBT-Connect.git
cd BUBT-Connect

# Windows
./scripts/install-git-config.ps1
# macOS / Linux
sh ./scripts/install-git-config.sh

# Create local.properties with your SDK path (never committed)
# sdk.dir=/path/to/Android/sdk

./gradlew assembleDebug          # build
./gradlew testDebugUnitTest      # unit tests
./gradlew installDebug           # install on a device/emulator
```

## Project layout

```
app/src/main/java/com/corethink/bubtconnect/
  MainActivity.kt          entry point
  ui/theme/                Color.kt, Theme.kt, Type.kt
app/src/test/              JVM unit tests
app/src/androidTest/       instrumented tests
.githooks/                 enforced git hooks
scripts/                   git config installers
docs/GIT_WORKFLOW.md       how we use git in this repo
```

## Contributing

Read [docs/GIT_WORKFLOW.md](docs/GIT_WORKFLOW.md) before your first commit. It
covers the one-time setup, branching, the commit message format, and the hooks
that will reject your commit if you get it wrong.

The short version:

```bash
git switch main && git pull --ff-only
git switch -c feat/short-description
# ... work ...
git add -p
git commit      # message must look like: feat(auth): add biometric unlock
git push -u origin HEAD
```

Commit messages follow Conventional Commits and are validated by a hook:

```
feat(auth): add biometric unlock

Why the change was needed. The diff already shows what changed.
```

## License

See [LICENSE](LICENSE).
