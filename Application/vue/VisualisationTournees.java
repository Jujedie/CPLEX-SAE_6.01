package Application.vue;

import Application.metier.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panneau de visualisation graphique des tournées VRP
 * 
 * @author Equipe 4
 */
public class VisualisationTournees extends JPanel
{
	private Solution solution;
	private int minX, maxX, minY, maxY;
	private static final Color[] COULEURS_TOURNEES = {
		new Color(231, 76, 60),   // Rouge
		new Color(52, 152, 219),  // Bleu
		new Color(46, 204, 113),  // Vert
		new Color(241, 196, 15),  // Jaune
		new Color(155, 89, 182),  // Violet
		new Color(230, 126, 34),  // Orange
		new Color(26, 188, 156),  // Turquoise
		new Color(189, 195, 199), // Gris
		new Color(192, 57, 43),   // Rouge foncé
		new Color(142, 68, 173)   // Violet foncé
	};
	
	public VisualisationTournees()
	{
		setPreferredSize(new Dimension(800, 600));
		setBackground(Color.WHITE);
	}
	
	public void setSolution(Solution solution)
	{
		this.solution = solution;
		calculerBornes();
		repaint();
	}
	
	/**
	 * Calcule les bornes min/max pour adapter l'affichage
	 */
	private void calculerBornes()
	{
		if (solution == null || solution.getTournees().isEmpty()) return;
		
		minX = Integer.MAX_VALUE;
		maxX = Integer.MIN_VALUE;
		minY = Integer.MAX_VALUE;
		maxY = Integer.MIN_VALUE;
		
		// Inclure le dépôt
		Client depot = solution.getDepot();
		minX = Math.min(minX, depot.x);
		maxX = Math.max(maxX, depot.x);
		minY = Math.min(minY, depot.y);
		maxY = Math.max(maxY, depot.y);
		
		// Parcourir tous les clients
		for (Tournee t : solution.getTournees())
		{
			for (Client c : t.getClients())
			{
				minX = Math.min(minX, c.x);
				maxX = Math.max(maxX, c.x);
				minY = Math.min(minY, c.y);
				maxY = Math.max(maxY, c.y);
			}
		}
		
		// Ajouter une marge
		int margeX = (maxX - minX) / 10;
		int margeY = (maxY - minY) / 10;
		minX -= margeX;
		maxX += margeX;
		minY -= margeY;
		maxY += margeY;
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		if (solution == null) return;
		
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		int marge = 50;
		int largeurDessin = getWidth() - 2 * marge;
		int hauteurDessin = getHeight() - 2 * marge;
		
		// Dessiner les tournées
		List<Tournee> tournees = solution.getTournees();
		for (int i = 0; i < tournees.size(); i++)
		{
			Tournee tournee = tournees.get(i);
			Color couleur = COULEURS_TOURNEES[i % COULEURS_TOURNEES.length];
			
			dessinerTournee(g2, tournee, couleur, marge, largeurDessin, hauteurDessin);
		}
		
		// Dessiner le dépôt en dernier pour qu'il soit au-dessus
		Client depot = solution.getDepot();
		Point pDepot = convertirCoordonnees(depot.x, depot.y, marge, largeurDessin, hauteurDessin);
		
		// Dépôt : carré rouge
		g2.setColor(new Color(192, 57, 43));
		g2.fillRect(pDepot.x - 8, pDepot.y - 8, 16, 16);
		g2.setColor(Color.BLACK);
		g2.setStroke(new BasicStroke(2));
		g2.drawRect(pDepot.x - 8, pDepot.y - 8, 16, 16);
		
		// Label "DÉPÔT"
		g2.setFont(new Font("Arial", Font.BOLD, 12));
		g2.drawString("DÉPÔT", pDepot.x + 12, pDepot.y + 5);
		
		// Afficher les statistiques
		afficherStatistiques(g2);
	}
	
	/**
	 * Dessine une tournée
	 */
	private void dessinerTournee(Graphics2D g2, Tournee tournee, Color couleur, int marge, int largeur, int hauteur)
	{
		if (tournee.getClients().isEmpty()) return;
		
		Client depot = solution.getDepot();
		Point pDepot = convertirCoordonnees(depot.x, depot.y, marge, largeur, hauteur);
		
		List<Client> clients = tournee.getClients();
		
		// Dessiner les arêtes
		g2.setColor(couleur);
		g2.setStroke(new BasicStroke(2));
		
		// Dépôt -> premier client
		Point p1 = convertirCoordonnees(clients.get(0).x, clients.get(0).y, marge, largeur, hauteur);
		dessinerFleche(g2, pDepot.x, pDepot.y, p1.x, p1.y);
		
		// Entre les clients
		for (int i = 0; i < clients.size() - 1; i++)
		{
			Point p = convertirCoordonnees(clients.get(i).x, clients.get(i).y, marge, largeur, hauteur);
			Point pNext = convertirCoordonnees(clients.get(i + 1).x, clients.get(i + 1).y, marge, largeur, hauteur);
			dessinerFleche(g2, p.x, p.y, pNext.x, pNext.y);
		}
		
		// Dernier client -> dépôt
		Point pDernier = convertirCoordonnees(clients.get(clients.size() - 1).x, clients.get(clients.size() - 1).y, marge, largeur, hauteur);
		dessinerFleche(g2, pDernier.x, pDernier.y, pDepot.x, pDepot.y);
		
		// Dessiner les clients
		for (Client client : clients)
		{
			Point p = convertirCoordonnees(client.x, client.y, marge, largeur, hauteur);
			
			// Cercle plein
			g2.setColor(couleur);
			g2.fillOval(p.x - 6, p.y - 6, 12, 12);
			
			// Contour noir
			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(1));
			g2.drawOval(p.x - 6, p.y - 6, 12, 12);
			
			// Numéro du client
			g2.setFont(new Font("Arial", Font.PLAIN, 9));
			String label = String.valueOf(client.number);
			FontMetrics fm = g2.getFontMetrics();
			int labelWidth = fm.stringWidth(label);
			g2.drawString(label, p.x - labelWidth / 2, p.y - 10);
		}
	}
	
	/**
	 * Dessine une flèche entre deux points
	 */
	private void dessinerFleche(Graphics2D g2, int x1, int y1, int x2, int y2)
	{
		// Ligne
		g2.drawLine(x1, y1, x2, y2);
		
		// Pointe de flèche
		double angle = Math.atan2(y2 - y1, x2 - x1);
		int longueurFleche = 8;
		int angleFleche = 25;
		
		int x3 = (int) (x2 - longueurFleche * Math.cos(angle - Math.toRadians(angleFleche)));
		int y3 = (int) (y2 - longueurFleche * Math.sin(angle - Math.toRadians(angleFleche)));
		int x4 = (int) (x2 - longueurFleche * Math.cos(angle + Math.toRadians(angleFleche)));
		int y4 = (int) (y2 - longueurFleche * Math.sin(angle + Math.toRadians(angleFleche)));
		
		g2.drawLine(x2, y2, x3, y3);
		g2.drawLine(x2, y2, x4, y4);
	}
	
	/**
	 * Convertit les coordonnées du problème en coordonnées d'écran
	 */
	private Point convertirCoordonnees(int x, int y, int marge, int largeur, int hauteur)
	{
		int rangeX = maxX - minX;
		int rangeY = maxY - minY;
		
		int screenX = marge + (int) ((x - minX) * largeur / (double) rangeX);
		int screenY = marge + hauteur - (int) ((y - minY) * hauteur / (double) rangeY);
		
		return new Point(screenX, screenY);
	}
	
	/**
	 * Affiche les statistiques dans un coin
	 */
	private void afficherStatistiques(Graphics2D g2)
	{
		g2.setFont(new Font("Arial", Font.BOLD, 14));
		g2.setColor(Color.BLACK);
		
		int x = 10;
		int y = 20;
		int lineHeight = 20;
		
		g2.drawString("Distance totale : " + String.format("%.2f", solution.calculerCoutTotal()), x, y);
		g2.drawString("Nombre de véhicules : " + solution.getNombreVehicules(), x, y + lineHeight);
		
		// Légende des couleurs
		y += lineHeight * 2;
		g2.setFont(new Font("Arial", Font.PLAIN, 11));
		List<Tournee> tournees = solution.getTournees();
		for (int i = 0; i < Math.min(tournees.size(), 10); i++)
		{
			Color couleur = COULEURS_TOURNEES[i % COULEURS_TOURNEES.length];
			g2.setColor(couleur);
			g2.fillRect(x, y - 8, 12, 12);
			g2.setColor(Color.BLACK);
			g2.drawRect(x, y - 8, 12, 12);
			
			Tournee t = tournees.get(i);
			String info = String.format("Tournée %d : %.2f (charge: %d)", 
				i + 1, t.calculerDistance(solution.getDepot()), t.getChargeActuelle());
			g2.drawString(info, x + 18, y + 3);
			y += lineHeight;
		}
	}
}
