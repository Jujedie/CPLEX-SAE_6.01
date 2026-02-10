package Application.metier;

import java.util.*;

/**
 * Classe contenant les opérateurs de voisinage pour le recuit simulé
 */
public class OperateursVoisinage
{
	private static Random random = new Random();
	
	/**
	 * Génère une solution voisine en appliquant un opérateur aléatoire
	 */
	public static Solution genererVoisin(Solution solution)
	{
		// Choisir aléatoirement un opérateur
		int operateur = random.nextInt(5);
		
		switch (operateur)
		{
			case 0: return swap2Clients(solution);
			case 1: return relocate(solution);
			case 2: return exchange(solution);
			case 3: return twoOpt(solution);
			default: return reverse(solution);
		}
	}
	
	/**
	 * Opérateur SWAP : échange deux clients dans la même tournée
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
		
		// Pas besoin de updateCharge() car on ne change pas la charge
		
		return copie;
	}
	
	/**
	 * Opérateur RELOCATE : déplace un client d'une tournée à une autre
	 */
	private static Solution relocate(Solution solution)
	{
		Solution copie = copierSolution(solution);
		List<Tournee> tournees = copie.getTournees();
		
		if (tournees.size() < 2) return copie;
		
		// Choisir deux tournées différentes
		int idx1 = random.nextInt(tournees.size());
		int idx2 = random.nextInt(tournees.size());
		while (idx1 == idx2) idx2 = random.nextInt(tournees.size());
		
		Tournee source = tournees.get(idx1);
		Tournee destination = tournees.get(idx2);
		
		if (source.getClients().isEmpty()) return copie;
		
		// Retirer un client aléatoire de la source
		int clientIdx = random.nextInt(source.getClients().size());
		Client client = source.getClients().get(clientIdx);
		
		// Vérifier si la destination peut accueillir ce client
		if (destination.peutAjouter(client))
		{
			source.getClients().remove(clientIdx);
			source.updateCharge();
			destination.getClients().add(client);
			destination.updateCharge();
		}
		
		return copie;
	}
	
	/**
	 * Opérateur EXCHANGE : échange deux clients entre deux tournées
	 */
	private static Solution exchange(Solution solution)
	{
		Solution copie = copierSolution(solution);
		List<Tournee> tournees = copie.getTournees();
		
		if (tournees.size() < 2) return copie;
		
		// Choisir deux tournées différentes non vides
		Tournee t1 = null, t2 = null;
		for (int tentative = 0; tentative < 10; tentative++)
		{
			int idx1 = random.nextInt(tournees.size());
			int idx2 = random.nextInt(tournees.size());
			if (idx1 != idx2 && !tournees.get(idx1).getClients().isEmpty() && 
			    !tournees.get(idx2).getClients().isEmpty())
			{
				t1 = tournees.get(idx1);
				t2 = tournees.get(idx2);
				break;
			}
		}
		
		if (t1 == null || t2 == null) return copie;
		
		int i = random.nextInt(t1.getClients().size());
		int j = random.nextInt(t2.getClients().size());
		
		Client c1 = t1.getClients().get(i);
		Client c2 = t2.getClients().get(j);
		
		// Vérifier les contraintes de capacité
		int nouvCapT1 = t1.getChargeActuelle() - c1.demand + c2.demand;
		int nouvCapT2 = t2.getChargeActuelle() - c2.demand + c1.demand;
		
		if (nouvCapT1 <= t1.getCapaciteMax() && nouvCapT2 <= t2.getCapaciteMax())
		{
			t1.getClients().set(i, c2);
			t2.getClients().set(j, c1);
			t1.updateCharge();
			t2.updateCharge();
		}
		
		return copie;
	}
	
	/**
	 * Opérateur 2-OPT : inverse un segment dans une tournée
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
