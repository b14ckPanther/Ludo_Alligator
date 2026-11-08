#!/usr/bin/env bash
# Builds dist/Ludo_Alligator.jar from the sources. Needs JDK 17 or newer.
# Maven is not required (mvn package also works and writes target/Ludo_Alligator.jar).
set -euo pipefail
cd "$(dirname "$0")"

BIN=""
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then
  BIN="$JAVA_HOME/bin/"
elif ! command -v javac >/dev/null 2>&1 && [[ -x /usr/libexec/java_home ]]; then
  BIN="$(/usr/libexec/java_home -v 17+)/bin/"
fi
"${BIN}javac" -version >/dev/null 2>&1 || { echo "javac not found. Install JDK 17+ or set JAVA_HOME."; exit 1; }

rm -rf build
mkdir -p build/classes dist

echo "Compiling..."
"${BIN}javac" --release 17 -encoding UTF-8 -d build/classes $(find src/main/java -name '*.java')
cp -R src/main/resources/. build/classes/ 2>/dev/null || true

echo "Packaging dist/Ludo_Alligator.jar..."
"${BIN}jar" --create --file dist/Ludo_Alligator.jar --main-class Controller.Main -C build/classes .

echo "Done. Start the game with ./run.sh or: java -jar dist/Ludo_Alligator.jar"
