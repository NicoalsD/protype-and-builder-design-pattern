#!/usr/bin/env bash
set -e

# System JRE (java-25-openjdk) is the headless variant -> GUI refuses to open.
# The VS Code Java extension bundles a full headful JDK 21; use it when present.
JDK="${JDK_HOME:-$HOME/.vscode/extensions/redhat.java-1.55.0-linux-x64/jre/21.0.11-linux-x86_64/bin}"

if [ ! -x "$JDK/javac" ]; then
    echo "JDK not found at $JDK. Set JDK_HOME to a full JDK, e.g. JDK_HOME=/path/to/jdk/bin ./run.sh"
    exit 1
fi

"$JDK/javac" -d out src/tour/*.java
"$JDK/java" -cp out tour.TourApp