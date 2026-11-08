#!/usr/bin/env bash
# Starts the game (macOS / Linux). Builds the jar first if it is missing.
set -euo pipefail
cd "$(dirname "$0")"

JAVA="java"
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" ]]; then
  JAVA="$JAVA_HOME/bin/java"
fi

[[ -f dist/Ludo_Alligator.jar ]] || ./build.sh
exec "$JAVA" -jar dist/Ludo_Alligator.jar "$@"
