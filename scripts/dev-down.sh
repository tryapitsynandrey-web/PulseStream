#!/usr/bin/env bash
set -euo pipefail

echo "========================================="
echo "Stopping PulseStream Developer Local Stack"
echo "========================================="

docker compose down -v

echo "Local environment is clean and completely stopped."
echo "========================================="
