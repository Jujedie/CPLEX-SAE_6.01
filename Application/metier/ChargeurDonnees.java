package Application.metier;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
		URL url = URI.create(urlString).toURL();
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
			String extention = cheminFichier.substring(cheminFichier.lastIndexOf(".") + 1).toLowerCase();
			if (!extention.equals("dat") && !extention.equals("txt"))
			{
				System.out.println("⚠️  Attention : le fichier n'a pas une extension .dat ou .txt, vérifiez que c'est un fichier VRP valide.");
			}

			if (extention.equals("dat"))
			{
				chargerOPLDepuisReader(reader);
			}
			else if (extention.equals("txt"))
			{
				chargerDepuisReader(reader);
			}
		}
	}

	/**
	 * Parse les données depuis un Reader
	 */
	private void chargerOPLDepuisReader(BufferedReader reader) throws IOException
	{
		clients.clear();

		this.nbClients = 0;
		this.capaciteVehicule = 0;
		this.meilleureSolutionConnue = 0.0;
		
		String ligne;
		while ((ligne = reader.readLine()) != null)
		{
			ligne = ligne.trim();
			if (ligne.startsWith("noeuds"))
			{
				int debutAccolade = ligne.indexOf('{');
				if (debutAccolade == -1)
				{
					continue;
				}
				int finAccolade = ligne.indexOf('}');
				if (finAccolade != -1) {
					String clients = ligne.substring(debutAccolade + 1, finAccolade).trim();
					for (String client : clients.split(","))
					{
						
						if (!client.trim().isEmpty() && !client.split("\"")[1].trim().equals("0"))
						{
							this.clients.add(new Client(Integer.parseInt(client.split("\"")[1].trim()), 0, (int)(Math.random() * 800), (int)(Math.random() * 600)));
							this.nbClients++;
						} else {
							this.depot = new Client(0, 0, (int)(Math.random() * 800), (int)(Math.random() * 600));
						}
					}
				}
			}
			if (ligne.startsWith("Qmax"))
			{
				this.capaciteVehicule = Integer.parseInt(ligne.split("=")[1].replace(";", "").trim());
			}
			if (ligne.startsWith("meilleurSolution"))
			{
				this.meilleureSolutionConnue = Double.parseDouble(ligne.split("=")[1].replace(";", "").trim());
			}

			if (ligne.startsWith("demande"))
			{
				if (ligne.indexOf('[') == -1 || ligne.indexOf(']') == -1)
				{
					continue;
				}

				String demandes = ligne.substring(ligne.indexOf('[') + 1, ligne.indexOf(']')).trim();
				String[] demandesArray = demandes.split(",");
				for (int i = 0; i < demandesArray.length && i < clients.size(); i++)
				{
					clients.get(i).demand = Integer.parseInt(demandesArray[i].trim());
				}
			}

			if (ligne.startsWith("distances"))
			{
				double[][] matrice = new double[this.nbClients+1][this.nbClients+1];

				System.out.println("Clients chargés : "+matrice.length);

				ligne = reader.readLine().trim();
				String[] distancesArray = ligne.substring(ligne.indexOf('[') + 1, ligne.indexOf(']')).split(",");
				for (int j = 0; j < distancesArray.length; j++)
				{
					matrice[0][j] = Double.parseDouble(distancesArray[j].trim());
					System.out.print(String.format("%.2f ", matrice[0][j])+", ");
				}

				for(int i = 0 ; i < this.nbClients; i++)
				{
					ligne = reader.readLine().trim();
					distancesArray = ligne.substring(ligne.indexOf('[') + 1, ligne.indexOf(']')).split(",");
					for (int j = 0; j < distancesArray.length; j++)
					{
						matrice[i+1][j] = Double.parseDouble(distancesArray[j].trim());
						
					}
				}
				System.out.println();

				Client.setMatriceDistances(matrice);
				break; // On peut arrêter la lecture après avoir trouvé la matrice de distances
			}
		}

		// Set depot as the first client (index 0)
		if (!clients.isEmpty())
		{
			depot = clients.get(0);
		}

		System.out.println("✓ Données OPL chargées : " + nbClients + " clients");
		System.out.println("  Capacité véhicule : " + capaciteVehicule);
		System.out.println("  Meilleure solution connue : " + meilleureSolutionConnue);
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
	public void calculerMatriceDistances()
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
