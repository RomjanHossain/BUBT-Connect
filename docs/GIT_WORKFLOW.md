# Git Workflow Guide

Read this before your first commit. Every rule here is enforced by a hook, so
you will hit them immediately. Getting them right once saves a lot of
`git commit --amend` later.

## 1. One-time setup

Clone, then run the installer from the repo root:

```powershell
# Windows (PowerShell)
./scripts/install-git-config.ps1
```

```bash
# macOS / Linux / Git Bash
sh ./scripts/install-git-config.sh
```

The installer is idempotent, so re-run it any time. It sets `core.hooksPath`,
`commit.template`, line-ending rules and the rest of the config described
below.

Set your identity once per machine:

```bash
git config --global user.name  "Your Name"
git config --global user.email "you@example.com"
```

Confirm the hooks are live:

```bash
git config core.hooksPath          # -> .githooks
git config commit.template         # -> .gitmessage
```

If `core.hooksPath` is not `.githooks`, the hooks are not running. Team members
each run the installer locally because Git config is per-clone, not shared.

## 2. Branching

`main` is protected. Never commit to it directly.

```bash
git switch main
git pull --ff-only
git switch -c feat/login-biometric     # always branch from an up-to-date main
```

Branch naming: `feat/`, `fix/`, `chore/`, `docs/`, `refactor/`, `hotfix/`
followed by a short kebab-case description.

```bash
feat/notifications-inbox
fix/crash-on-empty-course-list
chore/bump-compose-bom
```

Delete a branch after its PR merges:

```bash
git branch -d feat/login-biometric
git push origin --delete feat/login-biometric
```

## 3. Commit messages

We follow [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/).
`git commit` opens `.gitmessage` as a template so you do not have to remember the
shape.

```
<type>(<optional scope>): <subject>

<why, wrapped at 100 chars>
```

| Type | Use for |
| --- | --- |
| `feat` | new user-visible capability |
| `fix` | bug fix |
| `docs` | documentation only |
| `style` | formatting only, no behaviour change |
| `refactor` | restructuring, no new behaviour, no fix |
| `perf` | performance improvement |
| `test` | adding or fixing tests |
| `build` | Gradle, dependencies, build scripts |
| `ci` | CI config, hooks, tooling |
| `chore` | maintenance that fits nowhere else |
| `revert` | revert of a previous commit |

Scopes are optional and lowercase. Suggested for this project: `ui`, `theme`,
`nav`, `data`, `network`, `auth`, `gradle`, `ci`.

The `commit-msg` hook blocks anything that deviates:

- subject max 72 characters, lowercase start, no trailing period
- type must be in the table above, scope must be lowercase
- blank line required between subject and body
- no leftover `#` comment lines in the message
- `wip`, `tmp`, `asdf` and similar placeholders are rejected

Rejected:

```
fixed login bug
feat: Add Biometric Login
feat: add biometric login.
update everything
```

Accepted:

```
feat(auth): add biometric unlock

Fingerprint login removes the password step on the sign-in screen.
Keeps the password path for devices without enrolled biometrics.
```

Use `!` for a breaking change, which `git` interprets on version bumps:

```
feat(auth)!: drop the legacy password-only sign-in path
```

Merge, revert and `fixup!`/`squash!` commits skip the checks automatically.

### Atomic commits

One commit, one logical change. A reviewer should be able to revert a single
commit without breaking the build. Do not mix a Compose refactor with an
unrelated dependency bump.

Fix mistakes as you go instead of stacking more on top:

```bash
# stage only the file you actually touched
git add app/src/main/java/com/corethink/bubtconnect/ui/theme/Theme.kt

# stage one hunk from an interactive prompt
git add -p

# fold into the previous commit (message is not re-validated)
git commit --fixup <sha>
git rebase -i --autosquash main
```

## 4. Hooks

`.githooks/` holds four scripts, activated through `core.hooksPath`.

| Hook | What it does |
| --- | --- |
| `pre-commit` | blocks `local.properties`, keystores (`*.jks`, `*.keystore`, `*.p12`), `google-services.json`, `build/`, `.idea/`, `.gradle/`, `.apk`/`.aab`, and unresolved merge markers in staged text files |
| `commit-msg` | enforces the Conventional Commits rules above |
| `pre-push` | runs `./gradlew assembleDebug testDebugUnitTest` when app or Gradle files changed |
| `post-merge` | reports what was merged and warns if conflicts remain |

Emergency bypass, use sparingly and never as a habit:

```bash
git commit --no-verify
git push --no-verify
```

`pre-push` runs a real Gradle build, so it can take a minute or two. Turn it off
locally if that is too slow:

```bash
git config hooks.verifyPush false
```

Merge commits are always blocked from bypassing the hooks: `--no-verify` does
not apply to `git merge`.

## 5. Ignoring files

`.gitignore` already excludes build output, IDE state and secrets. If you add a
dependency or generated file that keeps showing up in `git status`, add the
pattern rather than deleting the file each time.

Note `gradle/wrapper/gradle-wrapper.jar` is intentionally tracked; the project
cannot build without it.

Never commit:

- `local.properties` (contains your local Android SDK path)
- keystores or any signing material
- `google-services.json` when it carries real credentials
- `build/` or `.gradle/` output

If a secret is already in history, removing the file is not enough. Rewrite the
history and rotate the credential.

## 6. Branching model

Short-lived branches merged into `main` through a pull request. Keep a branch
under a few days so it stays easy to review.

```bash
git switch -c feat/login-biometric
# ... work, commit in small pieces ...
git push -u origin feat/login-biometric
```

Open a PR, get one approval, then:

```bash
git switch main
git pull --ff-only
git merge --no-ff feat/login-biometric
git push origin main
git branch -d feat/login-biometric
```

`pull.ff` is set to `only`, so a plain `git pull` will never create a surprise
merge commit. Use `git pull --rebase` explicitly if you want to rebase your
local unpushed work.

## 7. Useful aliases

These are local-only. Add them with `git config --local alias.<name> "<cmd>"`.

```bash
git config --local alias.st  "status -sb"
git config --local alias.lg  "log --graph --oneline --decorate -20"
git config --local alias.last "log -1 HEAD --stat"
git config --local alias.unstage "restore --staged"
```

## 8. Daily checklist

```bash
git switch main && git pull --ff-only     # sync first
git switch -c feat/<short-description>   # branch
# edit, build, test
git add -p                               # stage deliberately
git commit                               # write a conventional message
git push -u origin HEAD                  # pre-push runs the Gradle build
```

## Troubleshooting

Hooks seem to do nothing:

```bash
git config core.hooksPath                # must print .githooks
ls -l .githooks                          # scripts must be executable (Linux/macOS)
sh scripts/install-git-config.sh          # re-run the installer
```

Line ending warnings on Windows (`LF will be replaced by CRLF`): the installer
sets `core.autocrlf false` plus `.gitattributes` so the repo stores LF and
`gradlew`/`*.sh` stay LF. Re-run the installer and, if a file is already
committed with the wrong endings, renormalize once:

```bash
git add --renormalize .
git commit -m "chore(git): normalize line endings"
```

Accidentally committed a secret:

```bash
git rm --cached local.properties          # or the keystore
git commit -m "chore(git): stop tracking local properties"
```

Then rotate the credential. It is still in history until you rewrite it.
