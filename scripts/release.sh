#!/usr/bin/env bash
# Cuts a new release: bumps versionName + versionCode together, commits, and tags.
# Does NOT push — review the commit/tag, then push yourself (this is deliberate: pushing a
# tag triggers the signed-build-and-publish workflow, a real user action, not an automatic one).
#
# Usage:
#   scripts/release.sh 0.3.0
#
# After it finishes:
#   git push && git push origin vX.Y.Z
# (or `git push --follow-tags` to do both in one command)

set -euo pipefail

VERSION="${1:-}"
if [[ -z "$VERSION" ]]; then
  echo "Usage: scripts/release.sh <version>   e.g. scripts/release.sh 0.3.0" >&2
  exit 1
fi
if [[ ! "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "Version must be X.Y.Z (e.g. 0.3.0), got: $VERSION" >&2
  exit 1
fi

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
GRADLE_FILE="$REPO_ROOT/android/app/build.gradle.kts"
TAG="v$VERSION"

if [[ -n "$(git -C "$REPO_ROOT" status --porcelain)" ]]; then
  echo "Working tree has uncommitted changes — commit or stash first." >&2
  exit 1
fi
if git -C "$REPO_ROOT" rev-parse "$TAG" >/dev/null 2>&1; then
  echo "Tag $TAG already exists." >&2
  exit 1
fi

CURRENT_CODE=$(grep -oP 'versionCode\s*=\s*\K[0-9]+' "$GRADLE_FILE")
CURRENT_NAME=$(grep -oP 'versionName\s*=\s*"\K[^"]+' "$GRADLE_FILE")
NEXT_CODE=$((CURRENT_CODE + 1))

if [[ "$CURRENT_NAME" == "$VERSION" ]]; then
  echo "versionName is already $VERSION — nothing to bump. Pick a new version." >&2
  exit 1
fi

echo "Bumping versionName $CURRENT_NAME -> $VERSION, versionCode $CURRENT_CODE -> $NEXT_CODE"

sed -i \
  -e "s/versionCode = $CURRENT_CODE/versionCode = $NEXT_CODE/" \
  -e "s/versionName = \"$CURRENT_NAME\"/versionName = \"$VERSION\"/" \
  "$GRADLE_FILE"

git -C "$REPO_ROOT" add "$GRADLE_FILE"
git -C "$REPO_ROOT" commit -m "Release $TAG"
git -C "$REPO_ROOT" tag -a "$TAG" -m "Prayer Tracker $TAG"

echo ""
echo "Done locally. Review with: git show HEAD"
echo "When ready, publish with:  git push --follow-tags"
echo "(pushing the tag triggers the GitHub Actions build + release)"
