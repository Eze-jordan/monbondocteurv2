#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")"

# Stop & remove containers
docker compose down

# Update code
git pull --ff-only

# (Optional) remove old image if it exists
docker image rm -f monbondocteurv2-app:latest 2>/dev/null || true

# Rebuild + start only app
docker compose up -d --build app
