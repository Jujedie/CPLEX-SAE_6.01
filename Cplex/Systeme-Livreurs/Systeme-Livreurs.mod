/*********************************************
 * OPL 22.1.1.0 Model - VRP Capacitaire
 * Systeme de Livraison (Cantine)
 *********************************************/

{string} noeuds = ...;
string depot = ...;
{string} clients = noeuds diff {depot};

int nbvehicules = ...;
int Qmax = ...;
int NbClients = ...;
int demande[clients] = ...;

float distances[noeuds][noeuds] = ...;

int N = card(noeuds);

// Variables de decision
dvar boolean x[noeuds][noeuds][1..nbvehicules];
dvar int u[clients] in 0..Qmax;

// Configuration CPLEX
execute PARAMS {
  cplex.tilim = 120;
  cplex.epgap = 0.01;
  cplex.threads = 6;
}

// Fonction objectif
minimize
  sum(i in noeuds, j in noeuds, k in 1..nbvehicules) x[i][j][k] * distances[i][j];

subject to {
  // Interdire les boucles
  forall(i in noeuds, k in 1..nbvehicules)
    x[i][i][k] == 0;

  // Chaque client visite une seule fois (entree)
  forall(j in clients)
    sum(k in 1..nbvehicules, i in noeuds: i != j) x[i][j][k] == 1;

  // Chaque client visite une seule fois (sortie)
  forall(i in clients)
    sum(k in 1..nbvehicules, j in noeuds: j != i) x[i][j][k] == 1;

  // Chaque vehicule part du depot au plus une fois
  forall(k in 1..nbvehicules)
    sum(j in clients) x[depot][j][k] == 1;

  // Chaque vehicule revient au depot au plus une fois
  forall(k in 1..nbvehicules)
    sum(i in clients) x[i][depot][k] == 1;

  // Conservation de flux
  forall(k in 1..nbvehicules, i in clients)
    sum(j in noeuds: j != i) x[i][j][k] == sum(j in noeuds: j != i) x[j][i][k];

  // Capacite par vehicule
  forall(k in 1..nbvehicules)
    sum(i in clients) demande[i] * sum(j in noeuds: j != i) x[i][j][k] <= Qmax;

  // MTZ (elimination des sous-tours)
  forall(i in clients, j in clients: i != j)
    u[i] - u[j] + Qmax * sum(k in 1..nbvehicules) x[i][j][k] <= Qmax - demande[j];

  forall(i in clients)
    u[i] >= demande[i];

  forall(i in clients)
    u[i] <= Qmax;
}

// Affichage des resultats
execute DISPLAY {
  writeln("=====================================");
  writeln("    SOLUTION VRP CAPACITAIRE");
  writeln("=====================================");
  writeln("");
  writeln("Distance totale : " + cplex.getObjValue());
  writeln("");
  writeln("Routes :");
  writeln("");
  
  for (var k = 1; k <= nbvehicules; k++) {
    var hasRoute = 0;
    for (var j in clients) {
      if (x[depot][j][k] == 1) {
        hasRoute = 1;
        break;
      }
    }
    
    if (hasRoute == 1) {
      write("Vehicule " + k + " : " + depot);
      var current = depot;
      var load = 0;
      var visited = 0;
      
      while (visited < N) {
        var found = 0;
        for (var next in noeuds) {
          if (x[current][next][k] == 1) {
            write(" -> " + next);
            if (next != depot) {
              load += demande[next];
            }
            current = next;
            found = 1;
            break;
          }
        }
        visited++;
        if (found == 0 || current == depot) break;
      }
      writeln("");
      writeln("  Charge : " + load + " / " + Qmax);
      writeln("");
    }
  }
  
  writeln("=====================================");
}