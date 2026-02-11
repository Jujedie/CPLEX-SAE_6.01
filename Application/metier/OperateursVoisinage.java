package Application.metier;

import java.util.*;

/**
 * Classe contenant les opérateurs de voisinage pour le recuit simulé
 * 
 * @author Equipe 4
 */
public class OperateursVoisinage
{
	private static Random random = new Random();
	
	/**
	 * Génère une solution voisine en appliquant un opérateur aléatoire
	 * 
	 * @param solution La solution de départ
	 * @return Une nouvelle solution voisine
	 */
	public static Solution genererVoisin(Solution solution)
	{
		// Choisir un opérateur avec des probabilités ajustées pour le VRP
		// Privilégier TWO-OPT pour réduire la distance, et EXCHANGE/RELOCATE pour le packing
		int r = random.nextInt(100);
		
		if (r < 30) return relocate(solution);      
		else if (r < 65) return exchange(solution); // 35% (Priorité augmentée sur l'échange intelligent)
		else if (r < 95) return twoOpt(solution);   // 30%
		else return swap2Clients(solution);         // 5%
	}
	
	/**
	 * Opérateur SWAP : échange deux clients dans la même tournée
	 * 
	 * @param solution La solution de départ
	 * @return Une nouvelle solution voisine
	 */
	private static Solution swap2Clients(Solution solution)
	{
		Solution copie = copierSolution(solution);
		List<Tournee> tournees = copie.getTournees();
		
		if (tournees.isEmpty()) return copie;
		
		// Choisir une tournée avec au moins 2 clients
		Tournee tournee = null;
		for (int tentative = 0; tentative < 10; tentative++)
		{
			Tournee t = tournees.get(random.nextInt(tournees.size()));
			if (t.getClients().size() >= 2)
			{
				tournee = t;
				break;
			}
		}
		
		if (tournee == null || tournee.getClients().size() < 2) return copie;
		
		List<Client> clients = tournee.getClients();
		int i = random.nextInt(clients.size());
		int j = random.nextInt(clients.size());
		while (i == j) j = random.nextInt(clients.size());
		
		// Échanger les clients
		Client temp = clients.get(i);
		clients.set(i, clients.get(j));
		clients.set(j, temp);
		
		return copie;
	}
	
	/**
	 * Opérateur RELOCATE : déplace un client d'une tournée à une autre
	 * 
	 * @param solution La solution de départ
	 * @return Une nouvelle solution voisine
	 */
	private static Solution relocate(Solution solution)
	{
		Solution copie = copierSolution(solution);
		List<Tournee> tournees = copie.getTournees();
		
		if (tournees.size() < 2) return copie;
		
		// Essayer plusieurs fois de trouver un mouvement valide
		for (int k = 0; k < 50; k++) {
			
			Tournee source = null;
			
			// Stratégie hybride : 
			// 50% du temps on essaie de vider la tournée la plus petite (pour réduire le nombre de véhicules)
			// 50% du temps on prend une source aléatoire
			if (random.nextBoolean()) {
				// Trouver la tournée avec le moins de clients (non vide)
				int minClients = Integer.MAX_VALUE;
				for (Tournee t : tournees) {
					if (!t.getClients().isEmpty() && t.getClients().size() < minClients) {
						minClients = t.getClients().size();
						source = t;
					}
				}
			}
			
			// Si pas trouvée ou cas aléatoire
			if (source == null) {
				int idx1 = random.nextInt(tournees.size());
				source = tournees.get(idx1);
			}

            // Choisir une destination différente de la source
			Tournee destination = null;
            int destIdx = random.nextInt(tournees.size());
            destination = tournees.get(destIdx);
            
            // Éviter source == destination
			if (source == destination) {
                // Essayer d'en prendre un autre
                destIdx = (destIdx + 1) % tournees.size();
                destination = tournees.get(destIdx);
                if (source == destination) continue; // Pas d'autre choix
            }
			
			if (source.getClients().isEmpty()) continue;
			
			// Retirer un client aléatoire de la source
			int clientIdx = random.nextInt(source.getClients().size());
			Client client = source.getClients().get(clientIdx);
			
			// Vérifier si la destination peut accueillir ce client
			// if (destination.peutAjouter(client)) -> RELACHEMENT DE LA CONTRAINTE
			if (true)
			{
				source.getClients().remove(clientIdx);
				source.updateCharge();
				
				// Si la source devient vide, on supprime la tournée (réduction de véhicules !)
				if (source.getClients().isEmpty()) {
					copie.getTournees().remove(source);
				}
				
				// Insertion aléatoire dans la destination
				int insertPos = 0;
				if (!destination.getClients().isEmpty()) {
					insertPos = random.nextInt(destination.getClients().size() + 1);
				}
				destination.getClients().add(insertPos, client);
				destination.updateCharge();
				
				return copie; // Mouvement réussi
			}
		}
		
		return copie; // Pas de mouvement valide trouvé
	}
	
	/**
	 * Opérateur EXCHANGE : échange deux clients entre deux tournées
	 * 
	 * @param solution La solution de départ
	 * @return Une nouvelle solution voisine
	 */
	private static Solution exchange(Solution solution)
	{
		Solution copie = copierSolution(solution);
		List<Tournee> tournees = copie.getTournees();
		
		if (tournees.size() < 2) return copie;
		
		// Paramètres de la recherche locale (Best Improvement sur échantillon)
		// Le sujet demande une taille max de voisinage de 2*n
		// On calcule n (nombre de clients)
		int n = 0;
		for(Tournee t : tournees) n += t.getClients().size();
		int tailleVoisinage = 2 * n;
		
		Client depot = solution.getDepot(); // On récupère le vrai dépôt pour les calculs de distance

		// Meilleur mouvement trouvé
		int bestIdx1 = -1, bestIdx2 = -1;
		int bestPos1 = -1, bestPos2 = -1;
		Client bestC1 = null, bestC2 = null;
		
		double bestDeltaCout = Double.MAX_VALUE; // On cherche à minimiser le coût
		boolean moveFound = false;
		
		for (int k = 0; k < tailleVoisinage; k++) {
			int idx1 = random.nextInt(tournees.size());
			int idx2 = random.nextInt(tournees.size());
			
			if (idx1 == idx2) continue;
			
			Tournee t1 = tournees.get(idx1);
			Tournee t2 = tournees.get(idx2);
			
			if (t1.getClients().isEmpty() || t2.getClients().isEmpty()) continue;
			
			int pos1 = random.nextInt(t1.getClients().size());
			int pos2 = random.nextInt(t2.getClients().size());
			
			Client c1 = t1.getClients().get(pos1);
			Client c2 = t2.getClients().get(pos2);
			
			// Vérification des capacités (Critère "garder tournées qui respectent capacités")
			// Adaptation : Si une tournée est validé, elle doit le rester. 
			// Si elle est invalide (stratégie penalty), on autorise si ça n'empire pas (ou si ça améliore le cout global).
			
			int charge1Avant = t1.getChargeActuelle();
			int charge2Avant = t2.getChargeActuelle();
			int cap1 = t1.getCapaciteMax();
			int cap2 = t2.getCapaciteMax();
			
			int charge1Apres = charge1Avant - c1.demand + c2.demand;
			int charge2Apres = charge2Avant - c2.demand + c1.demand;
			
			// Filtre de validité demandé par l'utilisateur
			boolean respecteCapacites = (charge1Apres <= cap1) && (charge2Apres <= cap2);
			
			// Si le mouvement viole la capacité, on l'ignore (selon la demande de l'utilisateur)
			if (!respecteCapacites) continue; 
			
			// Calcul du gain précis
			// 1. Calculer distance avant
			// Client depot = solution.getDepot(); // Déjà défini plus haut
			double distAvant = t1.calculerDistance(depot) + t2.calculerDistance(depot);
			
			// 2. Appliquer l'échange temporaire
			t1.getClients().set(pos1, c2);
			t2.getClients().set(pos2, c1);
			
			// 3. Calculer distance après
			double distApres = t1.calculerDistance(depot) + t2.calculerDistance(depot);
			double delta = distApres - distAvant;
			
			// 4. Vérifier si c'est le meilleur
			if (delta < bestDeltaCout) {
				bestDeltaCout = delta;
				bestIdx1 = idx1; bestIdx2 = idx2;
				bestPos1 = pos1; bestPos2 = pos2;
				bestC1 = c1; bestC2 = c2;
				moveFound = true;
			}
			
			// 5. Rollback immédiat
			t1.getClients().set(pos1, c1);
			t2.getClients().set(pos2, c2);
		}
		
		if (moveFound) {
			Tournee t1 = tournees.get(bestIdx1);
			Tournee t2 = tournees.get(bestIdx2);
			t1.getClients().set(bestPos1, bestC2);
			t2.getClients().set(bestPos2, bestC1);
			t1.updateCharge();
			t2.updateCharge();
		}
		
		return copie;
	}
	
	/**
	 * Opérateur 2-OPT : inverse un segment dans une tournée
	 * 
	 * @param solution La solution de départ
	 * @return Une nouvelle solution voisine
	 */
	private static Solution twoOpt(Solution solution)
	{
		Solution copie = copierSolution(solution);
		List<Tournee> tournees = copie.getTournees();
		
		if (tournees.isEmpty()) return copie;
		
		Tournee tournee = null;
		for (int tentative = 0; tentative < 10; tentative++)
		{
			Tournee t = tournees.get(random.nextInt(tournees.size()));
			if (t.getClients().size() >= 3)
			{
				tournee = t;
				break;
			}
		}
		
		if (tournee == null || tournee.getClients().size() < 3) return copie;
		
		List<Client> clients = tournee.getClients();
		int i = random.nextInt(clients.size() - 1);
		int j = i + 1 + random.nextInt(clients.size() - i - 1);
		
		// Inverser le segment [i, j]
		Collections.reverse(clients.subList(i, j + 1));
		
		return copie;
	}
	
	/**
	 * Opérateur REVERSE : inverse l'ordre des clients dans une tournée
	 * 
	 * @param solution La solution de départ
	 * @return Une nouvelle solution voisine
	 */
	private static Solution reverse(Solution solution)
	{
		Solution copie = copierSolution(solution);
		List<Tournee> tournees = copie.getTournees();
		
		if (tournees.isEmpty()) return copie;
		
		Tournee tournee = tournees.get(random.nextInt(tournees.size()));
		if (!tournee.getClients().isEmpty())
		{
			Collections.reverse(tournee.getClients());
		}
		
		return copie;
	}
	
	/**
	 * Copie profonde d'une solution
	 * 
	 * @param solution La solution à copier
	 * @return Une nouvelle solution identique mais indépendante
	 */
	private static Solution copierSolution(Solution solution)
	{
		Solution copie = new Solution(solution.getDepot());
		
		for (Tournee tournee : solution.getTournees())
		{
			Tournee nouvelleTournee = new Tournee(tournee.getCapaciteMax());
			for (Client client : tournee.getClients())
			{
				nouvelleTournee.ajouterClient(client);
			}
			copie.ajouterTournee(nouvelleTournee);
		}
		
		return copie;
	}
}
