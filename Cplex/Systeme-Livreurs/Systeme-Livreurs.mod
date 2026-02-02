/*********************************************
 * OPL 22.1.1.0 Model
 * Author: oj230564
 * Creation Date: 2 févr. 2026 at 09:33:55
 *********************************************/
{string} noeuds = ...;
string depot = ...; // un élément de "noeuds" servant de départ/arrivée
{string} clients = noeuds diff {depot};

int nbvéhicules = ...;
int Qmax = ...;
int NbClients = ...;
int demande[clients] = ...;

float distances[noeuds][noeuds] = ...;

int N = card(noeuds);

dvar boolean x[noeuds][noeuds][1..nbvéhicules];
dvar int u[clients] in 0..Qmax; // charge cumulée (MTZ) pour éliminer les sous-tours

// Configuration CPLEX pour améliorer les performances
execute PARAMS {
  cplex.tilim = 120;  // Limite de temps : 120 secondes
  cplex.epgap = 0.01; // Accepter une solution à 1% de l'optimal
  cplex.threads = 6;  // Utiliser 6 threads en parallèle
}

minimize
  sum(i in noeuds, j in noeuds, k in 1..nbvéhicules) x[i][j][k] * distances[i][j];

subject to {
  // Interdire les boucles (un noeud ne peut pas aller vers lui-même)
forall(i in noeuds, k in 1..nbvéhicules)
	x[i][i][k] == 0;

  // Chaque client est visité exactement une fois (entrée et sortie)
forall(i in clients)
	sum(k in 1..nbvéhicules, j in noeuds: j != i) x[i][j][k] == 1;

forall(j in clients)
	sum(k in 1..nbvéhicules, i in noeuds: i != j) x[i][j][k] == 1;

  // Chaque véhicule part une fois du dépôt et y revient
forall(k in 1..nbvéhicules)
	sum(j in clients) x[depot][j][k] == 1;

forall(k in 1..nbvéhicules)
	sum(i in clients) x[i][depot][k] == 1;

  // Conservation de flux pour chaque client et chaque véhicule
forall(k in 1..nbvéhicules, i in clients)
	sum(j in noeuds: j != i) x[i][j][k] == sum(j in noeuds: j != i) x[j][i][k];

  // Capacité par véhicule
forall(k in 1..nbvéhicules)
	sum(i in clients) demande[i] * sum(j in noeuds: j != i) x[i][j][k] <= Qmax;

  // MTZ capacitaire (élimination des sous-tours)
forall(i in clients)
	u[i] >= demande[i];

forall(i in clients)
	u[i] <= Qmax;

forall(i in clients, j in clients: i != j)
	u[i] - u[j] + Qmax * sum(k in 1..nbvéhicules) x[i][j][k] <= Qmax - demande[j];
}

execute {
writeln("===== Solution VRP capacitaire =====");
writeln("Distance totale minimale : ", cplex.getObjValue());
writeln("");

if (NbClients != card(clients)) {
	writeln("ATTENTION: NbClients (", NbClients, ") != nombre de clients (", card(clients), ")");
}

for (var k in 1..nbvéhicules) {
	writeln("Véhicule ", k, " :");
	var current = depot;
	var routeLoad = 0;
	write(current);

	var safety = 0;
	while (safety < N) {
	var found = 0;
	for (var next in noeuds) {
		if (x[current][next][k] == 1) {
		if (next != depot) {
			routeLoad += demande[next];
		}
		write(" -> " + next);
		current = next;
		found = 1;
		break;
		}
	}
	safety++;
	if (found == 0 || current == depot) {
		break;
	}
	}
	writeln("");
	writeln("  Charge totale : ", routeLoad, " / ", Qmax);
	writeln("");
}

// Affichage détaillé des arcs utilisés
writeln("=== Arcs utilisés ===");
var distanceTotale = 0;
for (var i in noeuds) {
	for (var j in noeuds) {
	for (var k in 1..nbvéhicules) {
		if (x[i][j][k] == 1) {
		writeln("  V", k, ": ", i, " -> ", j, " : distance = ", distances[i][j]);
		distanceTotale += distances[i][j];
		}
	}
	}
}
writeln("Distance totale (vérification) : ", distanceTotale);
writeln("");

// Affichage des valeurs MTZ (charges)
writeln("=== Charges cumulées (variables MTZ u) ===");
for (var node in clients) {
	writeln("  u[" + node + "] = " + u[node]);
}
writeln("");
writeln("===== Fin de la solution =====");
}