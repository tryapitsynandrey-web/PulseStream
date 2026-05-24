#!/usr/bin/env bash
set -euo pipefail

echo "========================================="
echo "Executing PulseStream Lint Checks"
echo "========================================="

if [ -d "/opt/homebrew/opt/openjdk@21" ]; then
    export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
fi

# Run checkstyle or compilation dry-run
mvn clean compile -DskipTests

echo "========================================="
echo "Code compilation and checks passed."
echo "========================================="
