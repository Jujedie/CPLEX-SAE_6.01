package Application.metier;

import java.util.*;

/**
 * Implémentation de l'algorithme de recuit simulé pour le VRP
 * 
 * @author Equipe 4
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
		this(5000.0, 0.01, 0.99, 1500, 5000);
	}
	
	/**
	 * Constructeur avec paramètres personnalisés
	 * 
	 * @param T0 Température initiale
	 * @param seuilArret Température de seuil d'arrêt
	 * @param alpha Coefficient de refroidissement (0 < alpha < 1)
	 * @param iterationsParTemp Nombre d'itérations par température
	 * @param maxSansAmelioration Nombre maximum d'itérations sans amélioration avant arrêt
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
	 * 
	 * @param solutionInitiale La solution de départ (généralement obtenue par une heuristique constructive)
	 * @return La meilleure solution trouvée après optimisation
	 */
	public Solution resoudre(Solution solutionInitiale)
	{
		tempsDebut = System.currentTimeMillis();
		
		// Initialisation
		solutionCourante = copierSolution(solutionInitiale);
		meilleureSolution = copierSolution(solutionInitiale);
		temperatureCourante = temperatureInitiale;
		iterationsSansAmelioration = 0;
		iterationTotale = 0;
		
		double coutCourant = solutionCourante.calculerCoutTotal();
		double meilleurCout = coutCourant;
		
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
					
					// Mise à jour de la meilleure solution
					if (coutCourant < meilleurCout)
					{
						meilleureSolution = copierSolution(solutionCourante);
						meilleurCout = coutCourant;
						iterationsSansAmelioration = 0;
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
		}
		
		tempsFin = System.currentTimeMillis();
		
		return meilleureSolution;
	}
	
	/**
	 * Copie une solution
	 * 
	 * @param solution La solution à copier
	 * @return Une nouvelle solution identique mais indépendante
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
