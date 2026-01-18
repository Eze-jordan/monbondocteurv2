#!/bin/bash

# Arrêter et supprimer tous les conteneurs du projet
docker compose down

# Supprimer l'image de l'application
docker rmi monbondocteurv2-net:latest

# Récupérer les dernières modifications depuis Git
git pull

# Reconstruire et relancer uniquement le service app (si tu veux tout relancer, retire `app`)
docker compose up -d --build app
