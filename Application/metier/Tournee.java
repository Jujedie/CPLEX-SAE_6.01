package Application.metier;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe représentant une tournée de véhicule dans le VRP
 * 
 * @author Equipe 4
 */
public class Tournee
{
	private List<Client> clients;
	private int chargeActuelle;
	private int capaciteMax;

	public Tournee(int capaciteMax)
	{
		this.clients = new ArrayList<>();
		this.chargeActuelle = 0;
		this.capaciteMax = capaciteMax;
	}

	public boolean peutAjouter(Client client)
	{
		return chargeActuelle + client.demand <= capaciteMax;
	}

	public void ajouterClient(Client client)
	{
		clients.add(client);
		chargeActuelle += client.demand;
	}

	public Client dernierClient()
	{
		return clients.isEmpty() ? null : clients.get(clients.size() - 1);
	}

	public double calculerDistance(Client depot)
	{
		if (clients.isEmpty())
			return 0;

		double distance = 0;
		// Distance du dépôt au premier client
		distance += depot.distanceTo(clients.get(0));

		// Distance entre les clients
		for (int i = 0; i < clients.size() - 1; i++)
		{
			distance += clients.get(i).distanceTo(clients.get(i + 1));
		}

		// Distance du dernier client au dépôt
		distance += clients.get(clients.size() - 1).distanceTo(depot);

		return distance;
	}

	public List<Client> getClients()
	{
		return clients; // Retourner la référence directe pour les opérateurs
	}

	public int getChargeActuelle()
	{
		return chargeActuelle;
	}
	
	public int getCapaciteMax()
	{
		return capaciteMax;
	}
	
	/**
	 * Recalcule la charge actuelle (utile après modification manuelle de la liste)
	 */
	public void updateCharge()
	{
		chargeActuelle = 0;
		for (Client c : clients)
		{
			chargeActuelle += c.demand;
		}
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("Tournée [charge: " + chargeActuelle + "/" + capaciteMax + "] : 0");
		for (Client c : clients)
		{
			sb.append(" -> ").append(c.number);
		}
		sb.append(" -> 0");
		return sb.toString();
	}
}