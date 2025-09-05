#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
echo "==> Running unit tests with test profile..."
mvn -B -Dspring.profiles.active=test -DskipTests=false test | cat
echo "==> Tests completed."
exit 0
