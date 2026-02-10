package Application.metier;

public class Client
{
	public int number;
	public int demand;
	public int x;
	public int y;
	
	// Matrice de distances statique (sera initialisée par Solution)
	private static double[][] matriceDistances;

	public Client(int number, int demand, int x, int y)
	{
		this.number = number;
		this.demand = demand;
		this.x = x;
		this.y = y;
	}
	
	public static void setMatriceDistances(double[][] matrice)
	{
		matriceDistances = matrice;
	}

	public double distanceTo(Client other)
	{
		// Si une matrice de distances est définie, l'utiliser
		if (matriceDistances != null && 
		    this.number < matriceDistances.length && 
		    other.number < matriceDistances[this.number].length)
		{
			return matriceDistances[this.number][other.number];
		}
		// Sinon, calculer la distance euclidienne (fallback)
		return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
	}

	@Override
	public String toString()
	{
		return "Client " + number;
	}
}