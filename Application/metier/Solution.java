package Application.metier;

import java.util.*;

// Classe représentant une solution complète
public  class Solution {
	private List<Tournee> tournees;
	private Client depot;
	
	public Solution(Client depot) {
		this.tournees = new ArrayList<>();
		this.depot = depot;
	}
	
	public void ajouterTournee(Tournee tournee) {
		if (!tournee.getClients().isEmpty()) {
			tournees.add(tournee);
		}
	}
	
	public double calculerCoutTotal() {
		double cout = 0;
		int depassementTotal = 0;
		
		for (Tournee t : tournees) {
			cout += t.calculerDistance(depot);
			int charge = t.getChargeActuelle();
			int cap = t.getCapaciteMax();
			if (charge > cap) {
				depassementTotal += (charge - cap);
			}
		}
		
		// Pénalité stricte pour guider vers une solution réalisable
		// Pour tai75a (10 camions pleins), il faut traverser des états infaisables
		return cout + (depassementTotal * 50.0);
	}
	
	public List<Tournee> getTournees() {
		return tournees;
	}
	
	public Client getDepot() {
		return depot;
	}
	
	public int getNombreVehicules() {
		return tournees.size();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("=== SOLUTION VRP ===\n");
		sb.append("Distance totale : ").append(String.format("%.2f", calculerCoutTotal())).append("\n");
		sb.append("Nombre de véhicules utilisés : ").append(getNombreVehicules()).append("\n\n");
		
		for (int i = 0; i < tournees.size(); i++) {
			sb.append("Véhicule ").append(i + 1).append(" : ");
			sb.append(tournees.get(i)).append("\n");
			sb.append("  Distance : ").append(String.format("%.2f", tournees.get(i).calculerDistance(depot))).append("\n");
		}
		
		return sb.toString();
	}
	
	
	/**
	 * Algorithme du plus proche voisin avec contrainte de capacité (heuristique constructive)
	 * Cette méthode construit une solution de manière gloutonne en ajoutant toujours
	 * le client le plus proche non encore visité, en respectant la contrainte de capacité
	 * 
	 * @param clients Liste de tous les clients (incluant le dépôt en position 0)
	 * @param capaciteVehicule Capacité maximale de chaque véhicule
	 * @return Une solution construite avec l'heuristique du plus proche voisin
	 */
	public static Solution heuristiquePlusProcheVoisin(List<Client> clients, int capaciteVehicule) {
		if (clients == null || clients.isEmpty()) {
			throw new IllegalArgumentException("La liste de clients ne peut pas être vide");
		}
		
		Client depot = clients.get(0); // Le dépôt est toujours le premier élément
		Solution solution = new Solution(depot);
		
		// Créer un ensemble des clients non visités (sans le dépôt)
		Set<Client> clientsNonVisites = new HashSet<>(clients.subList(1, clients.size()));
		
		// Tant qu'il reste des clients à visiter
		while (!clientsNonVisites.isEmpty()) {
			Tournee tourneeCourante = new Tournee(capaciteVehicule);
			Client positionCourante = depot;
			
			// Construire une tournée
			while (true) {
				// Trouver le client le plus proche non visité qui peut être ajouté
				Client plusProche = null;
				double distanceMin = Double.MAX_VALUE;
				
				for (Client client : clientsNonVisites) {
					if (tourneeCourante.peutAjouter(client)) {
						double distance = positionCourante.distanceTo(client);
						if (distance < distanceMin) {
							distanceMin = distance;
							plusProche = client;
						}
					}
				}
				
				// Si aucun client ne peut être ajouté, terminer cette tournée
				if (plusProche == null) {
					break;
				}
				
				// Ajouter le client le plus proche à la tournée
				tourneeCourante.ajouterClient(plusProche);
				clientsNonVisites.remove(plusProche);
				positionCourante = plusProche;
			}
			
			// Ajouter la tournée à la solution
			solution.ajouterTournee(tourneeCourante);
		}
		
		return solution;
	}
}
