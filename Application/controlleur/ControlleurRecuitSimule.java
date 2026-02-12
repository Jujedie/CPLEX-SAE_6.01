package Application.controlleur;

import Application.metier.*;
import Application.vue.InterfaceRecuitSimule;
import java.io.IOException;
import javax.swing.SwingUtilities;

/**
 * Contrôleur pour l'algorithme de recuit simulé
 * Fait le lien entre la vue (InterfaceRecuitSimule) et le métier (ChargeurDonnees, RecuitSimule)
 */
public class ControlleurRecuitSimule
{
	private InterfaceRecuitSimule vue;
	private ChargeurDonnees chargeur;
	private Thread threadOptimisation;
	private boolean enCours = false;
	
	public ControlleurRecuitSimule(InterfaceRecuitSimule vue)
	{
		this.vue = vue;
	}
	
	/**
	 * Charge les données depuis une URL
	 */
	public void chargerDepuisURL(String url) throws IOException
	{
		chargeur = new ChargeurDonnees();
		chargeur.chargerDepuisURL(url);
		
		vue.afficherInfosDonnees(
			chargeur.getNbClients(),
			chargeur.getCapaciteVehicule(),
			chargeur.getMeilleureSolutionConnue()
		);
		vue.logMessage("✓ Données chargées avec succès");
		vue.logMessage("  - " + chargeur.getNbClients() + " clients");
		vue.logMessage("  - Capacité : " + chargeur.getCapaciteVehicule());
		vue.logMessage("");
	}
	
	/**
	 * Charge les données depuis un fichier local
	 */
	public void chargerDepuisFichier(String chemin) throws IOException
	{
		chargeur = new ChargeurDonnees();
		chargeur.chargerDepuisFichier(chemin);
		
		vue.afficherInfosDonnees(
			chargeur.getNbClients(),
			chargeur.getCapaciteVehicule(),
			chargeur.getMeilleureSolutionConnue()
		);
		vue.logMessage("✓ Données chargées avec succès");
		vue.logMessage("  - " + chargeur.getNbClients() + " clients");
		vue.logMessage("  - Capacité : " + chargeur.getCapaciteVehicule());
		vue.logMessage("");
	}
	
	/**
	 * Démarre l'optimisation avec les paramètres fournis
	 */
	public void demarrerOptimisation(double T0, double seuil, double alpha, 
	                                  int iterParTemp, int maxSansAmelio)
	{
		if (chargeur == null)
		{
			vue.afficherErreur("Veuillez d'abord charger un fichier de données");
			return;
		}
		
		enCours = true;
		vue.activerModeOptimisation(true);
		
		// Lancer l'optimisation dans un thread séparé
		threadOptimisation = new Thread(() -> {
			try
			{
				long debut = System.currentTimeMillis();
				
				vue.logMessage("🔨 Génération de la solution initiale...");
				Solution initiale = Solution.heuristiquePlusProcheVoisin(
					chargeur.getClients(), 
					chargeur.getCapaciteVehicule()
				);
				vue.logMessage("✓ Solution initiale : " + String.format("%.2f", initiale.calculerCoutTotal()));
				vue.logMessage("");
				
				vue.logMessage("⚙️ Paramètres :");
				vue.logMessage("  T₀ = " + T0);
				vue.logMessage("  Seuil = " + seuil);
				vue.logMessage("  α = " + alpha);
				vue.logMessage("  Itérations/temp = " + iterParTemp);
				vue.logMessage("  Max sans amélioration = " + maxSansAmelio);
				vue.logMessage("");
				vue.logMessage("🚀 Démarrage de l'optimisation...");
				vue.logMessage("");
				
				RecuitSimule rs = new RecuitSimule(T0, seuil, alpha, iterParTemp, maxSansAmelio);
				
				// Callback pour mise à jour de l'interface
				rs.setProgressCallback((iter, temp, courante, meilleure, temps) -> {
					if (iter % 500 == 0)
					{
						vue.mettreAJourSolution(meilleure, temps);
						
						if (iter % 1000 == 0)
						{
							vue.logMessage(String.format("[%6d] T=%.4f | Courante: %.2f | Meilleure: %.2f", 
								iter, temp, courante.calculerCoutTotal(), meilleure.calculerCoutTotal()));
						}
					}
				});
				
				Solution optimisee = rs.resoudre(initiale);
				
				long fin = System.currentTimeMillis();
				double tempsTotal = (fin - debut) / 1000.0;
				
				// Mise à jour finale
				SwingUtilities.invokeLater(() -> {
					vue.afficherResultatFinal(optimisee, tempsTotal, chargeur.getMeilleureSolutionConnue());
				});
			}
			catch (Exception e)
			{
				SwingUtilities.invokeLater(() -> {
					vue.logMessage("❌ Erreur : " + e.getMessage());
					vue.afficherErreur("Erreur durant l'optimisation :\n" + e.getMessage());
				});
				e.printStackTrace();
			}
			finally
			{
				SwingUtilities.invokeLater(() -> {
					vue.activerModeOptimisation(false);
					enCours = false;
				});
			}
		});
		
		threadOptimisation.start();
	}
	
	/**
	 * Vérifie si des données sont chargées
	 */
	public boolean donneesChargees()
	{
		return chargeur != null;
	}
	
	/**
	 * Vérifie si une optimisation est en cours
	 */
	public boolean estEnCours()
	{
		return enCours;
	}
}
