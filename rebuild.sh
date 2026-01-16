#!/bin/bash
set -e

# Arrêter et supprimer tous les conteneurs du projet
docker compose down

# Supprimer l'image de l'application (optionnel)
docker rmi monbondocteurv2-app:latest 2>/dev/null || true

# Récupérer les dernières modifications depuis Git
git pull

# Reconstruire et relancer uniquement le service app
docker compose up -d --build app
