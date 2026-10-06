# Contributing to BUBT Connect

Full git workflow: [docs/GIT_WORKFLOW.md](docs/GIT_WORKFLOW.md).

## Before you start

Run the git config installer once per clone. It activates the hooks that will
otherwise reject your commits.

```powershell
./scripts/install-git-config.ps1   # Windows
```

```bash
sh ./scripts/install-git-config.sh # macOS / Linux
```

```bash
git config --global user.name  "Your Name"
git config --global user.email "you@example.com"
```

## Rules the hooks enforce

1. **Conventional Commits.** `type(scope): subject`, lowercase, subject max 72
   chars, no trailing period, blank line before the body.
2. **One logical change per commit.** A reviewer must be able to revert a single
   commit without breaking the build.
3. **No secrets or local files.** `local.properties`, `*.jks`, `*.keystore`,
   `*.p12`, `google-services.json` and build/IDE output are rejected by
   `pre-commit`.
4. **No unresolved merge markers** in staged text files.
5. **The build must pass before push.** `pre-push` runs
   `./gradlew assembleDebug testDebugUnitTest` when app or Gradle files changed.

Valid types: `feat fix docs style refactor perf test build ci chore revert`.

## Android specifics

- Versions go in `gradle/libs.versions.toml`, never inline in a build script.
- Keep `local.properties` local; it holds the SDK path.
- Add tests under `app/src/test` for logic and `app/src/androidTest` for UI.
- Run `./gradlew lintDebug` before opening a PR.

## Pull requests

- Branch from an up-to-date `main`, name it `feat/…`, `fix/…`, `chore/…`.
- Keep the branch short-lived and the diff focused.
- The PR description should state what changed and why, not restate the diff.

## Never use `--no-verify` as a habit

It exists for genuine emergencies, such as committing a hotfix from a machine
with no Android SDK installed. If a hook rejects a legitimate commit, fix the
hook or the message instead of routing around it.
