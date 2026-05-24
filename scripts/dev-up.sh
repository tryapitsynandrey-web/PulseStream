#!/usr/bin/env bash
set -euo pipefail

echo "========================================="
echo "Starting PulseStream Developer Local Stack"
echo "========================================="

docker compose up -d

echo "Waiting for PostgreSQL database to be healthy..."
until [ "$(docker inspect --format='{{json .State.Health.Status}}' pulsestream-db)" == "\"healthy\"" ]; do
    sleep 1
done
echo "PostgreSQL database is HEALTHY."

echo "Waiting for Apache Kafka broker to be healthy..."
until [ "$(docker inspect --format='{{json .State.Health.Status}}' pulsestream-kafka)" == "\"healthy\"" ]; do
    sleep 1
done
echo "Apache Kafka broker is HEALTHY."

echo "========================================="
echo "Local Environment is completely UP & RUNNING."
echo "- PostgreSQL: localhost:5432"
echo "- Kafka Bootstrap: localhost:9092"
echo "- Prometheus Dashboard: localhost:9090"
echo "- Grafana Dashboard: localhost:3000"
echo "========================================="
