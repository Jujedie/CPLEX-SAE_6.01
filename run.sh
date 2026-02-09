#!/bin/bash
# Script de lancement de l'interface graphique

cd "$(dirname "$0")"

# Vérifier si bin/ existe et contient des fichiers
if [ ! -d "bin" ] || [ -z "$(ls -A bin 2>/dev/null)" ]; then
    echo "Compilation nécessaire..."
    ./compile.sh
    if [ $? -ne 0 ]; then
        exit 1
    fi
    echo ""
fi

echo "╔════════════════════════════════════════════════════════════╗"
echo "║     Lancement Interface Recuit Simulé - VRP                ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

java -cp bin Application.vue.InterfaceRecuitSimule
