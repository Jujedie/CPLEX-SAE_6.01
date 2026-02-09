#!/bin/bash
# Script de compilation du projet VRP - Recuit Simulé

# Couleurs pour l'affichage
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "╔════════════════════════════════════════════════════════════╗"
echo "║     Compilation du projet VRP - Recuit Simulé              ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Aller dans le répertoire du script
cd "$(dirname "$0")"

# Créer le dossier bin s'il n'existe pas
if [ ! -d "bin" ]; then
    echo -e "${YELLOW}→${NC} Création du dossier bin/"
    mkdir -p bin
fi

# Nettoyer le dossier bin
echo -e "${YELLOW}→${NC} Nettoyage du dossier bin/"
rm -rf bin/*

# Rechercher tous les fichiers .java
JAVA_FILES=$(find Application Cplex -name "*.java" 2>/dev/null)

if [ -z "$JAVA_FILES" ]; then
    echo -e "${RED}✗${NC} Aucun fichier .java trouvé"
    exit 1
fi

# Compter les fichiers
NB_FILES=$(echo "$JAVA_FILES" | wc -l)
echo -e "${YELLOW}→${NC} $NB_FILES fichiers Java trouvés"
echo ""

# Compilation
echo -e "${YELLOW}→${NC} Compilation en cours..."
javac -d bin $JAVA_FILES 2>&1

# Vérifier le résultat
if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}✓${NC} Compilation réussie !"
    echo ""
    
    # Compter les fichiers .class générés
    NB_CLASS=$(find bin -name "*.class" | wc -l)
    echo -e "  ${NB_CLASS} fichiers .class générés dans bin/"
    echo ""
    
    # Afficher les points d'entrée principaux
    echo "Points d'entrée disponibles :"
    echo ""
    echo "  1. Interface graphique (Recuit Simulé) :"
    echo -e "     ${GREEN}java -cp bin Application.vue.InterfaceRecuitSimule${NC}"
    echo ""
    echo "  2. API complète (ligne de commande) :"
    echo -e "     ${GREEN}java -cp bin Application.APIRecuitSimule${NC}"
    echo ""
    echo "  3. Extracteur de données :"
    echo -e "     ${GREEN}java -cp bin InterfaceHommeMachineExtracteur${NC}"
    echo ""
    
    exit 0
else
    echo ""
    echo -e "${RED}✗${NC} Erreur de compilation"
    exit 1
fi
