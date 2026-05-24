#!/usr/bin/env bash
set -euo pipefail

echo "========================================="
echo "Executing PulseStream Complete Verification"
echo "========================================="

if [ -d "/opt/homebrew/opt/openjdk@21" ]; then
    export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
fi

# Automatically fallback to the macOS Docker Desktop socket if DOCKER_HOST is not set and the socket exists
if [ -z "${DOCKER_HOST:-}" ] && [ -S "${HOME}/.docker/run/docker.sock" ]; then
    export DOCKER_HOST="unix://${HOME}/.docker/run/docker.sock"
    echo "Auto-detected active macOS Docker socket. Setting DOCKER_HOST=${DOCKER_HOST}"
fi

mvn verify

echo "========================================="
echo "Integration verification passed successfully."
echo "========================================="
