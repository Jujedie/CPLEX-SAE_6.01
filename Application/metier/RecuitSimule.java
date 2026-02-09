package Application.metier;

import java.util.*;

/**
 * Implémentation de l'algorithme de recuit simulé pour le VRP
 */
public class RecuitSimule
{
	// Paramètres configurables
	private double temperatureInitiale;
	private double temperatureSeuilArret;
	private double alpha; // Coefficient de refroidissement
	private int iterationsParTemperature;
	private int maxIterationsSansAmelioration;
	
	// Variables de suivi
	private Solution solutionCourante;
	private Solution meilleureSolution;
	private double temperatureCourante;
	private int iterationsSansAmelioration;
	private int iterationTotale;
	private long tempsDebut;
	private long tempsFin;
	
	// Callback pour affichage
	private ProgressCallback callback;
	
	private Random random = new Random();
	
	public interface ProgressCallback
	{
		void onIteration(int iteration, double temperature, Solution courante, Solution meilleure, long tempsEcoule);
	}
	
	/**
	 * Constructeur avec paramètres par défaut
	 */
	public RecuitSimule()
	{
		this(10000.0, 0.01, 0.95, 100, 1000);
	}
	
	/**
	 * Constructeur avec paramètres personnalisés
	 */
	public RecuitSimule(double T0, double seuilArret, double alpha, int iterationsParTemp, int maxSansAmelioration)
	{
		this.temperatureInitiale = T0;
		this.temperatureSeuilArret = seuilArret;
		this.alpha = alpha;
		this.iterationsParTemperature = iterationsParTemp;
		this.maxIterationsSansAmelioration = maxSansAmelioration;
	}
	
	/**
	 * Configure un callback pour suivre la progression
	 */
	public void setProgressCallback(ProgressCallback callback)
	{
		this.callback = callback;
	}
	
	/**
	 * Exécute l'algorithme de recuit simulé
	 */
	public Solution resoudre(Solution solutionInitiale)
	{
		System.out.println("\n╔════════════════════════════════════════════════════════════╗");
		System.out.println("║         ALGORITHME DE RECUIT SIMULÉ - VRP                 ║");
		System.out.println("╚════════════════════════════════════════════════════════════╝");
		System.out.println("\nParamètres :");
		System.out.println("  • Température initiale (T₀)        : " + temperatureInitiale);
		System.out.println("  • Seuil d'arrêt (Tₘᵢₙ)              : " + temperatureSeuilArret);
		System.out.println("  • Coefficient α                     : " + alpha);
		System.out.println("  • Itérations par température       : " + iterationsParTemperature);
		System.out.println("  • Max itérations sans amélioration : " + maxIterationsSansAmelioration);
		System.out.println();
		
		tempsDebut = System.currentTimeMillis();
		
		// Initialisation
		solutionCourante = copierSolution(solutionInitiale);
		meilleureSolution = copierSolution(solutionInitiale);
		temperatureCourante = temperatureInitiale;
		iterationsSansAmelioration = 0;
		iterationTotale = 0;
		
		double coutCourant = solutionCourante.calculerCoutTotal();
		double meilleurCout = coutCourant;
		
		System.out.println("Solution initiale : " + String.format("%.2f", coutCourant));
		System.out.println("\nDémarrage de l'optimisation...\n");
		
		// DEBUG : Compteurs
		int nbAcceptes = 0;
		int nbAmeliorations = 0;
		int nbVoisinsDifferents = 0;
		
		// Boucle principale
		while (temperatureCourante > temperatureSeuilArret && 
		       iterationsSansAmelioration < maxIterationsSansAmelioration)
		{
			for (int i = 0; i < iterationsParTemperature; i++)
			{
				iterationTotale++;
				
				// Générer une solution voisine
				Solution voisin = OperateursVoisinage.genererVoisin(solutionCourante);
				double coutVoisin = voisin.calculerCoutTotal();
				
				// DEBUG : Vérifier si le voisin est différent
				if (Math.abs(coutVoisin - coutCourant) > 0.001)
				{
					nbVoisinsDifferents++;
				}
				
				// Calcul de la variation de coût
				double delta = coutVoisin - coutCourant;
				
				// Critère d'acceptation
				boolean accepter = false;
				if (delta < 0)
				{
					// Amélioration : toujours accepter
					accepter = true;
				}
				else
				{
					// Dégradation : accepter avec probabilité exp(-delta/T)
					double probabilite = Math.exp(-delta / temperatureCourante);
					accepter = random.nextDouble() < probabilite;
				}
				
				if (accepter)
				{
					solutionCourante = voisin;
					coutCourant = coutVoisin;
					nbAcceptes++;
					
					// Mise à jour de la meilleure solution
					if (coutCourant < meilleurCout)
					{
						meilleureSolution = copierSolution(solutionCourante);
						meilleurCout = coutCourant;
						iterationsSansAmelioration = 0;
						nbAmeliorations++;
					}
					else
					{
						iterationsSansAmelioration++;
					}
				}
				else
				{
					iterationsSansAmelioration++;
				}
				
				// Callback pour affichage périodique
				if (callback != null && iterationTotale % 100 == 0)
				{
					long tempsEcoule = System.currentTimeMillis() - tempsDebut;
					callback.onIteration(iterationTotale, temperatureCourante, solutionCourante, meilleureSolution, tempsEcoule);
				}
			}
			
			// Refroidissement
			temperatureCourante *= alpha;
			
			// Affichage périodique avec statistiques
			if (iterationTotale % 1000 == 0)
			{
				long tempsEcoule = (System.currentTimeMillis() - tempsDebut) / 1000;
				System.out.printf("Itération %6d | T=%.4f | Courante: %.2f | Meilleure: %.2f | Temps: %ds | Voisins diff: %d | Acceptés: %d | Améliorations: %d%n",
					iterationTotale, temperatureCourante, coutCourant, meilleurCout, tempsEcoule, 
					nbVoisinsDifferents, nbAcceptes, nbAmeliorations);
				// Reset compteurs
				nbVoisinsDifferents = 0;
				nbAcceptes = 0;
				nbAmeliorations = 0;
			}
		}
		
		tempsFin = System.currentTimeMillis();
		
		// Résultats finaux
		afficherResultatsFinaux();
		
		return meilleureSolution;
	}
	
	/**
	 * Affiche les résultats finaux de l'optimisation
	 */
	private void afficherResultatsFinaux()
	{
		double tempsExecution = (tempsFin - tempsDebut) / 1000.0;
		
		System.out.println("\n╔════════════════════════════════════════════════════════════╗");
		System.out.println("║                    RÉSULTATS FINAUX                        ║");
		System.out.println("╚════════════════════════════════════════════════════════════╝");
		System.out.println("\nCritère d'arrêt atteint :");
		if (temperatureCourante <= temperatureSeuilArret)
		{
			System.out.println("  → Température minimale atteinte (T = " + String.format("%.6f", temperatureCourante) + ")");
		}
		else
		{
			System.out.println("  → Pas d'amélioration depuis " + maxIterationsSansAmelioration + " itérations");
		}
		
		System.out.println("\nStatistiques :");
		System.out.println("  • Nombre total d'itérations : " + iterationTotale);
		System.out.println("  • Temps d'exécution         : " + String.format("%.2f", tempsExecution) + " secondes");
		System.out.println("  • Meilleur coût trouvé      : " + String.format("%.2f", meilleureSolution.calculerCoutTotal()));
		System.out.println("  • Nombre de véhicules       : " + meilleureSolution.getNombreVehicules());
		System.out.println();
	}
	
	/**
	 * Copie une solution
	 */
	private Solution copierSolution(Solution solution)
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
	
	// Getters pour les résultats
	public Solution getMeilleureSolution() { return meilleureSolution; }
	public double getTempsExecution() { return (tempsFin - tempsDebut) / 1000.0; }
	public int getIterationsTotales() { return iterationTotale; }
	
	// Setters pour les paramètres
	public void setTemperatureInitiale(double t0) { this.temperatureInitiale = t0; }
	public void setTemperatureSeuilArret(double seuil) { this.temperatureSeuilArret = seuil; }
	public void setAlpha(double alpha) { this.alpha = alpha; }
	public void setIterationsParTemperature(int iter) { this.iterationsParTemperature = iter; }
	public void setMaxIterationsSansAmelioration(int max) { this.maxIterationsSansAmelioration = max; }
}
