#!/bin/sh
#
# Installs the repository Git configuration and hooks (macOS / Linux / Git Bash).
# Safe to re-run.

set -e

repo_root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$repo_root"

if [ ! -d .githooks ]; then
	echo "error: .githooks not found in $repo_root" >&2
	exit 1
fi

# --- Hooks -------------------------------------------------------------------
git config core.hooksPath .githooks
chmod +x .githooks/* 2>/dev/null || true

# --- Line endings ------------------------------------------------------------
git config core.autocrlf false
git config core.eol lf
git config core.safecrlf warn

# --- Safety defaults ---------------------------------------------------------
git config core.ignorecase true
git config core.precomposeunicode true
git config core.bigFileThreshold 5m

# --- Pull / push -------------------------------------------------------------
git config pull.ff only
git config push.default current
git config push.autoSetupRemote true
git config fetch.prune true
git config fetch.pruneTags true

# --- Diff / merge ------------------------------------------------------------
git config color.diff auto
git config diff.colorMoved default
git config diff.algorithm histogram
git config merge.conflictstyle zdiff3
git config rerere.enabled true
git config rerere.autoUpdate true

# --- Commit messages ---------------------------------------------------------
git config commit.verbose true
git config commit.cleanup scissors
git config commit.template .gitmessage
git config core.commentChar '#'

# --- Status / misc -----------------------------------------------------------
git config status.showUntrackedFiles normal
git config log.date relative

printf '\nGit configuration installed:\n'
printf '  core.hooksPath  = .githooks\n'
printf '  commit.template = .gitmessage\n'
printf '  core.autocrlf   = false\n\n'
printf 'Set your identity once per machine if not already done:\n'
printf '  git config --global user.name  "Your Name"\n'
printf '  git config --global user.email "you@example.com"\n'
