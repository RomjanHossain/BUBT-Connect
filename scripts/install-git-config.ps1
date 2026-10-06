<#
.SYNOPSIS
	Installs the repository Git configuration and hooks (Windows / PowerShell).

.DESCRIPTION
	Sets the local Git settings this project expects, points core.hooksPath at
	.githooks and makes the hook scripts executable. Safe to re-run.

.EXAMPLE
	./scripts/install-git-config.ps1
#>

$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
if (-not (Test-Path -LiteralPath (Join-Path $repoRoot '.githooks'))) {
	Write-Error "Could not find .githooks at $repoRoot. Run this script from inside the repo."
}

Push-Location $repoRoot

# --- Hooks -------------------------------------------------------------------
git config core.hooksPath .githooks

# On Windows, without this Git for Windows may not execute the hook scripts.
if (-not (Get-Command bash -ErrorAction SilentlyContinue)) {
	Write-Warning 'bash not found on PATH; POSIX hooks require Git Bash.'
}

# --- Line endings: repository stores LF, checkout uses the platform default ---
git config core.autocrlf false
git config core.eol lf
git config core.safecrlf warn

# --- Identity-friendly / safety defaults -------------------------------------
git config core.ignorecase true
git config core.precomposeunicode true
git config core.bigFileThreshold 5m

# --- Pull / push behaviour ---------------------------------------------------
git config pull.ff only
git config push.default current
git config push.autoSetupRemote true
git config fetch.prune true
git config fetch.pruneTags true

# --- Diff / merge niceties ---------------------------------------------------
git config diff.colorMoved default
git config diff.algorithm histogram
git config color.diff auto
git config merge.conflictstyle zdiff3
git config rerere.enabled true
git config rerere.autoUpdate true

# --- Commit messages ---------------------------------------------------------
git config commit.verbose true
git config commit.cleanup scissors
git config commit.template .gitmessage
git config core.commentChar '#'
# Warn before rewriting published history.
git config branch.main.pushRemote origin

# --- Status / misc -----------------------------------------------------------
git config status.showUntrackedFiles normal
git config log.date relative

Pop-Location

Write-Host ''
Write-Host 'Git configuration installed:' -ForegroundColor Green
Write-Host '  core.hooksPath   = .githooks'
Write-Host '  commit.template  = .gitmessage'
Write-Host '  core.autocrlf    = false'
Write-Host ''
Write-Host 'Set your identity once per machine if not already done:' -ForegroundColor Yellow
Write-Host '  git config --global user.name  "Your Name"'
Write-Host '  git config --global user.email "you@example.com"'
