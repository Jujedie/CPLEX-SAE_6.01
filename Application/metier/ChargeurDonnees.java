package Application.metier;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Classe pour charger les données depuis un fichier VRP (local ou URL)
 */
public class ChargeurDonnees
{
	private int nbClients;
	private double meilleureSolutionConnue;
	private int capaciteVehicule;
	private Client depot;
	private List<Client> clients;
	
	public ChargeurDonnees()
	{
		this.clients = new ArrayList<>();
	}
	
	/**
	 * Charge les données depuis une URL
	 */
	public void chargerDepuisURL(String urlString) throws IOException
	{
		URL url = new URL(urlString);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream())))
		{
			chargerDepuisReader(reader);
		}
	}
	
	/**
	 * Charge les données depuis un fichier local
	 */
	public void chargerDepuisFichier(String cheminFichier) throws IOException
	{
		try (BufferedReader reader = new BufferedReader(new FileReader(cheminFichier)))
		{
			chargerDepuisReader(reader);
		}
	}
	
	/**
	 * Parse les données depuis un Reader
	 */
	private void chargerDepuisReader(BufferedReader reader) throws IOException
	{
		clients.clear();
		
		// Ligne 1 : nombre de clients et meilleure solution connue
		String ligne = reader.readLine().trim();
		String[] parts = ligne.split("\\s+");
		nbClients = Integer.parseInt(parts[0]);
		meilleureSolutionConnue = Double.parseDouble(parts[1]);
		
		// Ligne 2 : capacité du véhicule
		ligne = reader.readLine().trim();
		capaciteVehicule = Integer.parseInt(ligne);
		
		// Ligne 3 : coordonnées du dépôt
		ligne = reader.readLine().trim();
		parts = ligne.split("\\s+");
		int depotX = Integer.parseInt(parts[0]);
		int depotY = Integer.parseInt(parts[1]);
		depot = new Client(0, 0, depotX, depotY);
		clients.add(depot);
		
		// Lignes suivantes : clients (numéro, x, y, demande)
		for (int i = 0; i < nbClients; i++)
		{
			ligne = reader.readLine().trim();
			parts = ligne.split("\\s+");
			
			int numero = Integer.parseInt(parts[0]);
			int x = Integer.parseInt(parts[1]);
			int y = Integer.parseInt(parts[2]);
			int demande = Integer.parseInt(parts[3]);
			
			clients.add(new Client(numero, demande, x, y));
		}
		
		System.out.println("✓ Données chargées : " + nbClients + " clients");
		System.out.println("  Capacité véhicule : " + capaciteVehicule);
		System.out.println("  Meilleure solution connue : " + meilleureSolutionConnue);
		
		// Calculer et initialiser la matrice de distances euclidiennes
		calculerMatriceDistances();
	}
	
	/**
	 * Calcule la matrice de distances euclidiennes entre tous les clients
	 */
	private void calculerMatriceDistances()
	{
		int n = clients.size();
		double[][] matrice = new double[n][n];
		
		for (int i = 0; i < n; i++)
		{
			for (int j = 0; j < n; j++)
			{
				if (i == j)
				{
					matrice[i][j] = 0;
				}
				else
				{
					Client c1 = clients.get(i);
					Client c2 = clients.get(j);
					double dx = c1.x - c2.x;
					double dy = c1.y - c2.y;
					matrice[i][j] = Math.sqrt(dx * dx + dy * dy);
				}
			}
		}
		
		// Initialiser la matrice statique dans Client
		Client.setMatriceDistances(matrice);
		System.out.println("  Matrice de distances calculée (" + n + "x" + n + ")");
	}
	
	public int getNbClients() { return nbClients; }
	public double getMeilleureSolutionConnue() { return meilleureSolutionConnue; }
	public int getCapaciteVehicule() { return capaciteVehicule; }
	public Client getDepot() { return depot; }
	public List<Client> getClients() { return new ArrayList<>(clients); }
}
