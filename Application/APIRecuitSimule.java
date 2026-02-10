package Application;

import Application.metier.*;
import Application.vue.VisualisationTournees;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * API principale pour l'algorithme de recuit simulé sur le VRP
 * Démonstration avec le fichier tai75a.txt
 */
public class APIRecuitSimule
{
	public static void main(String[] args)
	{
		System.out.println("╔════════════════════════════════════════════════════════════╗");
		System.out.println("║     API RECUIT SIMULÉ - VEHICLE ROUTING PROBLEM            ║");
		System.out.println("╚════════════════════════════════════════════════════════════╝\n");
		
		try
		{
			// ═══════════════════════════════════════════════════════════
			// 1. CHARGEMENT DES DONNÉES depuis URL
			// ═══════════════════════════════════════════════════════════
			System.out.println("📥 Chargement des données depuis l'URL...");
			ChargeurDonnees chargeur = new ChargeurDonnees();
			
			String url = "https://mistic.iict-heig-vd.ch/taillard/problemes.dir/vrp.dir/taillard.dir/tai75a.txt";
			chargeur.chargerDepuisURL(url);
			
			System.out.println();
			
			// ═══════════════════════════════════════════════════════════
			// 2. GÉNÉRATION DE LA SOLUTION INITIALE
			// ═══════════════════════════════════════════════════════════
			System.out.println("🔨 Génération de la solution initiale (plus proche voisin)...");
			Solution solutionInitiale = Solution.heuristiquePlusProcheVoisin(
				chargeur.getClients(), 
				chargeur.getCapaciteVehicule()
			);
			
			System.out.println("✓ Solution initiale générée");
			System.out.println("  Distance : " + String.format("%.2f", solutionInitiale.calculerCoutTotal()));
			System.out.println("  Véhicules : " + solutionInitiale.getNombreVehicules());
			System.out.println();
			
			// ═══════════════════════════════════════════════════════════
			// 3. CONFIGURATION DES PARAMÈTRES DU RECUIT SIMULÉ
			// ═══════════════════════════════════════════════════════════
			double T0 = 100000.0;                    // Température initiale très élevée
			double seuilArret = 0.01;               // Seuil de température pour arrêt
			double alpha = 0.95;                    // Coefficient de refroidissement (0.1 à 0.9)
			int iterationsParTemp = 100;            // Itérations par palier de température
			int maxSansAmelioration = 5000;         // Critère d'arrêt : itérations sans amélioration
			
			RecuitSimule recuitSimule = new RecuitSimule(T0, seuilArret, alpha, iterationsParTemp, maxSansAmelioration);
			
			// ═══════════════════════════════════════════════════════════
			// 4. CONFIGURATION DU CALLBACK POUR AFFICHAGE EN TEMPS RÉEL
			// ═══════════════════════════════════════════════════════════
			recuitSimule.setProgressCallback((iteration, temperature, courante, meilleure, tempsEcoule) -> {
				// Afficher toutes les 500 itérations
				if (iteration % 500 == 0)
				{
					System.out.printf("  [%6d] T=%.2f | Courante: %.2f | Meilleure: %.2f | %ds%n",
						iteration, temperature, 
						courante.calculerCoutTotal(), 
						meilleure.calculerCoutTotal(),
						tempsEcoule / 1000);
				}
			});
			
			// ═══════════════════════════════════════════════════════════
			// 5. EXÉCUTION DE L'ALGORITHME
			// ═══════════════════════════════════════════════════════════
			Solution solutionOptimisee = recuitSimule.resoudre(solutionInitiale);
			
			// ═══════════════════════════════════════════════════════════
			// 6. AFFICHAGE DES RÉSULTATS DÉTAILLÉS
			// ═══════════════════════════════════════════════════════════
			System.out.println("\n" + solutionOptimisee);
			
			System.out.println("📊 Comparaison avec la meilleure solution connue :");
			System.out.println("  • Meilleure connue : " + chargeur.getMeilleureSolutionConnue());
			System.out.println("  • Solution trouvée : " + String.format("%.2f", solutionOptimisee.calculerCoutTotal()));
			double gap = ((solutionOptimisee.calculerCoutTotal() - chargeur.getMeilleureSolutionConnue()) 
			              / chargeur.getMeilleureSolutionConnue()) * 100;
			System.out.println("  • Gap              : " + String.format("%.2f", gap) + "%");
			System.out.println();
			
			// ═══════════════════════════════════════════════════════════
			// 7. AFFICHAGE GRAPHIQUE DES TOURNÉES
			// ═══════════════════════════════════════════════════════════
			afficherVisualisationGraphique(solutionOptimisee, chargeur);
			
		}
		catch (IOException e)
		{
			System.err.println("❌ Erreur lors du chargement des données : " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Affiche la visualisation graphique des tournées
	 */
	private static void afficherVisualisationGraphique(Solution solution, ChargeurDonnees chargeur)
	{
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Visualisation des Tournées VRP - Recuit Simulé");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(1000, 700);
			
			VisualisationTournees panel = new VisualisationTournees();
			panel.setSolution(solution);
			
			// Panel d'informations
			JPanel infoPanel = new JPanel();
			infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
			infoPanel.setBorder(BorderFactory.createTitledBorder("Informations"));
			
			infoPanel.add(new JLabel("Distance totale : " + String.format("%.2f", solution.calculerCoutTotal())));
			infoPanel.add(new JLabel("Nombre de véhicules : " + solution.getNombreVehicules()));
			infoPanel.add(new JLabel("Meilleure connue : " + chargeur.getMeilleureSolutionConnue()));
			infoPanel.add(new JLabel("Nombre de clients : " + chargeur.getNbClients()));
			
			frame.setLayout(new BorderLayout());
			frame.add(panel, BorderLayout.CENTER);
			frame.add(infoPanel, BorderLayout.SOUTH);
			
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			
			System.out.println("🖼️  Fenêtre graphique ouverte (fermez-la pour terminer le programme)");
		});
	}
	
	/**
	 * Exemple avec paramètres personnalisés
	 */
	public static Solution resoudreAvecParametres(
		String urlOuFichier, 
		double T0, 
		double seuilArret, 
		double alpha,
		int maxSansAmelioration)
	{
		try
		{
			ChargeurDonnees chargeur = new ChargeurDonnees();
			
			// Détecter si c'est une URL ou un fichier local
			if (urlOuFichier.startsWith("http"))
			{
				chargeur.chargerDepuisURL(urlOuFichier);
			}
			else
			{
				chargeur.chargerDepuisFichier(urlOuFichier);
			}
			
			Solution initiale = Solution.heuristiquePlusProcheVoisin(
				chargeur.getClients(), 
				chargeur.getCapaciteVehicule()
			);
			
			RecuitSimule rs = new RecuitSimule(T0, seuilArret, alpha, 100, maxSansAmelioration);
			return rs.resoudre(initiale);
		}
		catch (IOException e)
		{
			System.err.println("Erreur : " + e.getMessage());
			return null;
		}
	}
}
