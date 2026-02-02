/*********************************************
 * OPL 22.1.1.0 Model
 * Author: oj230564
 * Creation Date: 2 févr. 2026 at 09:33:55
 *********************************************/
{string} noeuds = ...;
string depot = ...; // un élément de "noeuds" servant de départ/arrivée

int distances[noeuds][noeuds] = ...;

int N = card(noeuds);

dvar boolean x[noeuds][noeuds];
dvar int u[noeuds] in 0..N-1; // variable auxiliaire pour les contraintes MTZ (bornée entre 0 et N-1)

// Configuration CPLEX pour améliorer les performances
execute PARAMS {
  cplex.tilim = 120;  // Limite de temps : 120 secondes
  cplex.epgap = 0.01; // Accepter une solution à 1% de l'optimal
  cplex.threads = 6;  // Utiliser 6 threads en parallèle
}

minimize
  sum(i in noeuds, j in noeuds) x[i][j] * distances[i][j];

subject to {
  // Interdire les boucles (un noeud ne peut pas aller vers lui-même)
  forall(i in noeuds) 
    x[i][i] == 0;

  // Degré sortant = 1 pour chaque sommet
  forall(i in noeuds)
    sum(j in noeuds: i != j) x[i][j] == 1;

  // Degré entrant = 1 pour chaque sommet
  forall(j in noeuds)
    sum(i in noeuds: i != j) x[i][j] == 1;

  // Miller-Tucker-Zemlin (MTZ) : contraintes d'élimination de sous-tours
  //forall(i in noeuds, j in noeuds: i != j && i != depot && j != depot)
   // u[i] - u[j] + N * x[i][j] <= N - 1;

  // Le dépôt a la position 0
  u[depot] == 0;
  
  // Les autres noeuds ont des positions entre 1 et N-1
  forall(i in noeuds: i != depot) 
    u[i] >= 1;
}

execute {
  writeln("===== Solution TSP avec MTZ =====");
  writeln("Distance totale minimale : ", cplex.getObjValue());
  writeln("");
  
  // Affichage du chemin complet
  writeln("Chemin optimal :");
  var current = depot;
  var visited = 0;
  write(current);
  
  while (visited < N - 1) {
    var found = 0;
    for (var next in noeuds) {
      if (x[current][next] == 1) {
        write(" -> " + next);
        current = next;
        visited++;
        found = 1;
        break;
      }
    }
    if (found == 0) {
      writeln("\nERREUR: Pas de suivant trouvé pour " + current);
      break;
    }
  }
  writeln(" -> " + depot);
  writeln("");
  
  // Affichage détaillé des arcs utilisés
  writeln("=== Arcs utilisés ===");
  var distanceTotale = 0;
  for (var i in noeuds) {
    for (var j in noeuds) {
      if (x[i][j] == 1) {
        writeln("  " + i + " -> " + j + " : distance = " + distances[i][j]);
        distanceTotale += distances[i][j];
      }
    }
  }
  writeln("Distance totale (vérification) : " + distanceTotale);
  writeln("");
  
  // Affichage des valeurs MTZ (ordre de visite)
  writeln("=== Ordre de visite (variables MTZ u) ===");
  for (var node in noeuds) {
    writeln("  u[" + node + "] = " + u[node]);
  }
  writeln("");
  writeln("===== Fin de la solution =====");
}