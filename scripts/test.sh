#!/usr/bin/env bash
set -euo pipefail

echo "========================================="
echo "Executing PulseStream Unit Tests"
echo "========================================="

# Enforce Java 21 if openjdk@21 is on local brew path
if [ -d "/opt/homebrew/opt/openjdk@21" ]; then
    export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
fi

mvn clean test

echo "========================================="
echo "Unit testing passed successfully."
echo "========================================="
