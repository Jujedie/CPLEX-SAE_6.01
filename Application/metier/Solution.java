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
		for (Tournee t : tournees) {
			cout += t.calculerDistance(depot);
		}
		return cout;
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
	
	/**
	 * Méthode de test pour démontrer l'utilisation de l'algorithme
	 * Utilise les données réelles du fichier Systeme-Livreurs-Cantines.dat
	 */
	public static void main(String[] args) {
		// Matrice de distances du fichier Systeme-Livreurs-Cantines.dat
		double[][] matriceDistances = {
			{0, 2.7, 4.6, 2.8, 3, 3.3, 3.1, 2.7, 5.1, 3.9, 4.7},
			{2.7, 0, 3.1, 0.8, 1.8, 2.5, 4.2, 1.4, 3.6, 2.5, 3},
			{4.6, 3.1, 0, 3.3, 4.4, 1.7, 6.8, 4.1, 1.3, 1.7, 1.4},
			{2.8, 0.8, 3.3, 0, 1.9, 2, 4, 1.5, 3.8, 2.8, 3.2},
			{3, 1.8, 4.4, 1.9, 0, 3.4, 2.6, 0.5, 4.7, 4.7, 4.1},
			{3.3, 2.5, 1.7, 2, 3.4, 0, 5.8, 3, 1.8, 0.5, 2.6},
			{3.1, 4.2, 6.8, 4, 2.6, 5.8, 0, 3, 7.4, 6.1, 7.6},
			{2.7, 1.4, 4.1, 1.5, 0.5, 3, 3, 0, 4.6, 3.7, 4.3},
			{5.1, 3.6, 1.3, 3.8, 4.7, 1.8, 7.4, 4.6, 0, 1.4, 2.8},
			{3.9, 2.5, 1.7, 2.8, 4.7, 0.5, 6.1, 3.7, 1.4, 0, 2.8},
			{4.7, 3, 1.4, 3.2, 4.1, 2.6, 7.6, 4.3, 2.8, 2.8, 0}
		};
		
		// Initialiser la matrice de distances dans la classe Client
		Client.setMatriceDistances(matriceDistances);
		
		// Données réelles du fichier Systeme-Livreurs-Cantines.dat
		// noeuds: S (dépôt), A, B, C, D, E, F, G, H, I, J
		// Demandes: [60, 18, 26, 15, 44, 32, 20, 10, 27, 11]
		
		List<Client> clients = new ArrayList<>();
		
		// Dépôt S (position 0, demande 0)
		clients.add(new Client(0, 0, 0, 0));
		
		// Clients A à J avec leurs demandes (x et y ne sont plus utilisés)
		clients.add(new Client(1, 60, 0, 0));   // A - demande: 60
		clients.add(new Client(2, 18, 0, 0));   // B - demande: 18
		clients.add(new Client(3, 26, 0, 0));   // C - demande: 26
		clients.add(new Client(4, 15, 0, 0));   // D - demande: 15
		clients.add(new Client(5, 44, 0, 0));   // E - demande: 44
		clients.add(new Client(6, 32, 0, 0));   // F - demande: 32
		clients.add(new Client(7, 20, 0, 0));   // G - demande: 20
		clients.add(new Client(8, 10, 0, 0));   // H - demande: 10
		clients.add(new Client(9, 27, 0, 0));   // I - demande: 27
		clients.add(new Client(10, 11, 0, 0));  // J - demande: 11
		
		int capaciteVehicule = 100;
		
		System.out.println("=== TEST HEURISTIQUE PLUS PROCHE VOISIN ===");
		System.out.println("Données: Systeme-Livreurs-Cantines\n");
		System.out.println("Nombre de clients : " + (clients.size() - 1));
		System.out.println("Capacité des véhicules : " + capaciteVehicule);
		System.out.println("Demande totale : " + (60+18+26+15+44+32+20+10+27+11) + "\n");
		
		Solution solution = heuristiquePlusProcheVoisin(clients, capaciteVehicule);
		System.out.println(solution);
	}
}
