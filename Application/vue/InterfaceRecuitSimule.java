package Application.vue;

import Application.metier.*;
import Application.controlleur.ControlleurRecuitSimule;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

/**
 * Interface graphique complète pour l'algorithme de recuit simulé
 * Vue MVC - communique uniquement avec le contrôleur
 */
public class InterfaceRecuitSimule extends JFrame
{
	// Composants de configuration
	private JTextField txtFichier;
	private JButton btnParcourir;
	private JButton btnChargerURL;
	
	// Paramètres du recuit simulé
	private JSpinner spinT0;
	private JSpinner spinSeuil;
	private JSpinner spinAlpha;
	private JSpinner spinIterParTemp;
	private JSpinner spinMaxSansAmelio;
	
	// Contrôles
	private JButton btnDemarrer;
	private JButton btnArreter;
	
	// Affichage des résultats
	private JTextArea txtLog;
	private VisualisationTournees panelVisu;
	
	// Labels d'information
	private JLabel lblNbClients;
	private JLabel lblCapacite;
	private JLabel lblMeilleurConnue;
	private JLabel lblStatut;
	private JLabel lblTempsExecution;
	private JLabel lblMeilleureDistance;
	
	// Contrôleur
	private ControlleurRecuitSimule controlleur;
	private Solution solutionCourante;
	
	public InterfaceRecuitSimule()
	{
		super("Recuit Simulé - VRP (Vehicle Routing Problem)");
		controlleur = new ControlleurRecuitSimule(this);
		initComponents();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1400, 900);
		setLocationRelativeTo(null);
	}
	
	private void initComponents()
	{
		setLayout(new BorderLayout(10, 10));
		
		// Panel principal gauche (configuration)
		JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
		leftPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		leftPanel.setPreferredSize(new Dimension(450, 0));
		
		// Panel de sélection de fichier
		leftPanel.add(creerPanelFichier(), BorderLayout.NORTH);
		
		// Panel central avec paramètres et contrôles
		JPanel centerLeft = new JPanel(new BorderLayout(5, 5));
		
		// Panel du haut (paramètres + contrôles)
		JPanel topControls = new JPanel(new BorderLayout(5, 5));
		topControls.add(creerPanelParametres(), BorderLayout.NORTH);
		topControls.add(creerPanelControles(), BorderLayout.SOUTH);
		
		centerLeft.add(topControls, BorderLayout.NORTH);
		centerLeft.add(creerPanelLog(), BorderLayout.CENTER);
		
		leftPanel.add(centerLeft, BorderLayout.CENTER);
		
		// Panel droit (visualisation)
		JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
		rightPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		panelVisu = new VisualisationTournees();
		panelVisu.setBorder(new TitledBorder("Visualisation des tournées"));
		
		rightPanel.add(panelVisu, BorderLayout.CENTER);
		rightPanel.add(creerPanelStatistiques(), BorderLayout.SOUTH);
		
		// Assemblage
		add(leftPanel, BorderLayout.WEST);
		add(rightPanel, BorderLayout.CENTER);
	}
	
	private JPanel creerPanelFichier()
	{
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBorder(new TitledBorder("Fichier de données"));
		
		JPanel topPanel = new JPanel(new BorderLayout(5, 5));
		
		txtFichier = new JTextField();
		txtFichier.setEditable(false);
		
		btnParcourir = new JButton("📁 Parcourir");
		btnParcourir.addActionListener(e -> parcourirFichier());
		
		topPanel.add(txtFichier, BorderLayout.CENTER);
		topPanel.add(btnParcourir, BorderLayout.EAST);
		
		btnChargerURL = new JButton("🌐 Charger tai75a.txt depuis Internet");
		btnChargerURL.addActionListener(e -> chargerDepuisURL());
		
		JPanel infoPanel = new JPanel(new GridLayout(3, 2, 5, 5));
		infoPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		
		infoPanel.add(new JLabel("Nombre de clients :"));
		lblNbClients = new JLabel("-");
		infoPanel.add(lblNbClients);
		
		infoPanel.add(new JLabel("Capacité véhicule :"));
		lblCapacite = new JLabel("-");
		infoPanel.add(lblCapacite);
		
		infoPanel.add(new JLabel("Meilleure connue :"));
		lblMeilleurConnue = new JLabel("-");
		infoPanel.add(lblMeilleurConnue);
		
		panel.add(topPanel, BorderLayout.NORTH);
		panel.add(btnChargerURL, BorderLayout.CENTER);
		panel.add(infoPanel, BorderLayout.SOUTH);
		
		return panel;
	}
	
	private JPanel creerPanelParametres()
	{
		JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
		panel.setBorder(new TitledBorder("Paramètres du recuit simulé"));
		panel.setBorder(BorderFactory.createCompoundBorder(
			new TitledBorder("Paramètres du recuit simulé"),
			new EmptyBorder(10, 10, 10, 10)
		));
		
		// Température initiale
		panel.add(new JLabel("Température initiale (T₀) :"));
		spinT0 = new JSpinner(new SpinnerNumberModel(10000.0, 100.0, 100000.0, 1000.0));
		panel.add(spinT0);
		
		// Seuil d'arrêt
		panel.add(new JLabel("Seuil d'arrêt (Tₘᵢₙ) :"));
		spinSeuil = new JSpinner(new SpinnerNumberModel(0.01, 0.0001, 10.0, 0.01));
		panel.add(spinSeuil);
		
		// Alpha
		panel.add(new JLabel("Coefficient α (refroidissement) :"));
		spinAlpha = new JSpinner(new SpinnerNumberModel(0.95, 0.1, 0.99, 0.01));
		panel.add(spinAlpha);
		
		// Itérations par température
		panel.add(new JLabel("Itérations par température :"));
		spinIterParTemp = new JSpinner(new SpinnerNumberModel(100, 10, 1000, 10));
		panel.add(spinIterParTemp);
		
		// Max sans amélioration
		panel.add(new JLabel("Max itérations sans amélioration :"));
		spinMaxSansAmelio = new JSpinner(new SpinnerNumberModel(5000, 100, 50000, 500));
		panel.add(spinMaxSansAmelio);
		
		return panel;
	}
	
	private JPanel creerPanelControles()
	{
		JPanel panel = new JPanel(new BorderLayout(3, 3));
		panel.setBorder(BorderFactory.createCompoundBorder(
			new TitledBorder("Contrôles"),
			new EmptyBorder(5, 5, 5, 5)
		));
		
		JPanel btnPanel = new JPanel(new GridLayout(2, 1, 3, 3));
		
		btnDemarrer = new JButton("▶ Démarrer");
		btnDemarrer.setEnabled(false);
		btnDemarrer.setFont(new Font("Arial", Font.BOLD, 11));
		btnDemarrer.setBackground(new Color(46, 204, 113));
		btnDemarrer.setForeground(Color.WHITE);
		btnDemarrer.setFocusPainted(false);
		btnDemarrer.addActionListener(e -> demarrerOptimisation());
		
		btnArreter = new JButton("⬛ Arrêter");
		btnArreter.setEnabled(false);
		btnArreter.setFont(new Font("Arial", Font.BOLD, 11));
		btnArreter.setBackground(new Color(231, 76, 60));
		btnArreter.setForeground(Color.WHITE);
		btnArreter.setFocusPainted(false);
		btnArreter.addActionListener(e -> arreterOptimisation());
		
		btnPanel.add(btnDemarrer);
		btnPanel.add(btnArreter);
		
		lblStatut = new JLabel("En attente", JLabel.CENTER);
		lblStatut.setFont(new Font("Arial", Font.PLAIN, 10));
		
		panel.add(btnPanel, BorderLayout.CENTER);
		panel.add(lblStatut, BorderLayout.SOUTH);
		
		return panel;
	}
	
	private JPanel creerPanelLog()
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(new TitledBorder("Journal d'exécution"));
		
		txtLog = new JTextArea();
		txtLog.setEditable(false);
		txtLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
		
		JScrollPane scroll = new JScrollPane(txtLog);
		panel.add(scroll, BorderLayout.CENTER);
		
		return panel;
	}
	
	private JPanel creerPanelStatistiques()
	{
		JPanel panel = new JPanel(new GridLayout(2, 2, 10, 5));
		panel.setBorder(BorderFactory.createCompoundBorder(
			new TitledBorder("Résultats"),
			new EmptyBorder(5, 10, 5, 10)
		));
		
		panel.add(new JLabel("Temps d'exécution :"));
		lblTempsExecution = new JLabel("-");
		lblTempsExecution.setFont(new Font("Arial", Font.BOLD, 12));
		panel.add(lblTempsExecution);
		
		panel.add(new JLabel("Meilleure distance :"));
		lblMeilleureDistance = new JLabel("-");
		lblMeilleureDistance.setFont(new Font("Arial", Font.BOLD, 12));
		lblMeilleureDistance.setForeground(new Color(46, 204, 113));
		panel.add(lblMeilleureDistance);
		
		return panel;
	}
	
	private void parcourirFichier()
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new javax.swing.filechooser.FileFilter()
		{
			public boolean accept(File f)
			{
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt");
			}
			
			public String getDescription()
			{
				return "Fichiers VRP (*.txt)";
			}
		});
		
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			File fichier = chooser.getSelectedFile();
			txtFichier.setText(fichier.getAbsolutePath());
			chargerFichier(fichier.getAbsolutePath(), false);
		}
	}
	
	private void chargerDepuisURL()
	{
		String url = "https://mistic.iict-heig-vd.ch/taillard/problemes.dir/vrp.dir/taillard.dir/tai75a.txt";
		txtFichier.setText(url);
		chargerFichier(url, true);
	}
	
	private void chargerFichier(String chemin, boolean estURL)
	{
		try
		{
			logMessage("📥 Chargement des données...");
			
			if (estURL)
			{
				controlleur.chargerDepuisURL(chemin);
			}
			else
			{
				controlleur.chargerDepuisFichier(chemin);
			}
			
			btnDemarrer.setEnabled(true);
		}
		catch (IOException e)
		{
			afficherErreur("Erreur lors du chargement du fichier :\n" + e.getMessage());
			logMessage("❌ Erreur : " + e.getMessage());
		}
	}
	
	private void demarrerOptimisation()
	{
		// Récupérer les paramètres
		double T0 = (Double) spinT0.getValue();
		double seuil = (Double) spinSeuil.getValue();
		double alpha = (Double) spinAlpha.getValue();
		int iterParTemp = (Integer) spinIterParTemp.getValue();
		int maxSansAmelio = (Integer) spinMaxSansAmelio.getValue();
		
		// Déléguer au contrôleur
		controlleur.demarrerOptimisation(T0, seuil, alpha, iterParTemp, maxSansAmelio);
	}
	
	private void arreterOptimisation()
	{
		controlleur.arreterOptimisation();
	}
	
	// ========== Méthodes publiques appelées par le contrôleur ==========
	
	/**
	 * Affiche les informations sur les données chargées
	 */
	public void afficherInfosDonnees(int nbClients, int capacite, double meilleureSolutionConnue)
	{
		SwingUtilities.invokeLater(() -> {
			lblNbClients.setText(String.valueOf(nbClients));
			lblCapacite.setText(String.valueOf(capacite));
			lblMeilleurConnue.setText(String.format("%.2f", meilleureSolutionConnue));
		});
	}
	
	/**
	 * Active ou désactive le mode optimisation
	 */
	public void activerModeOptimisation(boolean enCours)
	{
		SwingUtilities.invokeLater(() -> {
			btnDemarrer.setEnabled(!enCours);
			btnArreter.setEnabled(enCours);
			btnParcourir.setEnabled(!enCours);
			btnChargerURL.setEnabled(!enCours);
			spinT0.setEnabled(!enCours);
			spinSeuil.setEnabled(!enCours);
			spinAlpha.setEnabled(!enCours);
			spinIterParTemp.setEnabled(!enCours);
			spinMaxSansAmelio.setEnabled(!enCours);
			
			if (enCours)
			{
				lblStatut.setText("⏳ En cours");
				txtLog.setText("");
			}
		});
	}
	
	/**
	 * Met à jour la solution affichée
	 */
	public void mettreAJourSolution(Solution solution, long tempsMs)
	{
		SwingUtilities.invokeLater(() -> {
			solutionCourante = solution;
			panelVisu.setSolution(solution);
			lblMeilleureDistance.setText(String.format("%.2f", solution.calculerCoutTotal()));
			lblTempsExecution.setText(String.format("%.1f s", tempsMs / 1000.0));
		});
	}
	
	/**
	 * Affiche le résultat final de l'optimisation
	 */
	public void afficherResultatFinal(Solution optimisee, double tempsTotal, double meilleureSolutionConnue)
	{
		SwingUtilities.invokeLater(() -> {
			solutionCourante = optimisee;
			panelVisu.setSolution(optimisee);
			lblMeilleureDistance.setText(String.format("%.2f", optimisee.calculerCoutTotal()));
			lblTempsExecution.setText(String.format("%.2f s", tempsTotal));
			lblStatut.setText("✅ Terminé");
			
			logMessage("");
			logMessage("═══════════════════════════════════════");
			logMessage("✓ OPTIMISATION TERMINÉE");
			logMessage("═══════════════════════════════════════");
			logMessage("Distance finale : " + String.format("%.2f", optimisee.calculerCoutTotal()));
			logMessage("Véhicules : " + optimisee.getNombreVehicules());
			logMessage("Temps total : " + String.format("%.2f s", tempsTotal));
			
			double gap = ((optimisee.calculerCoutTotal() - meilleureSolutionConnue) 
			              / meilleureSolutionConnue) * 100;
			logMessage("Gap vs meilleure connue : " + String.format("%.2f%%", gap));
			
			logMessage("");
			logMessage("📋 Détail des tournées :");
			logMessage("─────────────────────────────────────");
			
			for (int i = 0; i < optimisee.getTournees().size(); i++)
			{
				Tournee t = optimisee.getTournees().get(i);
				double distTournee = t.calculerDistance(optimisee.getDepot());
				
				logMessage(String.format("Véhicule %2d [charge: %4d/%4d] [distance: %.2f]", 
					i + 1, t.getChargeActuelle(), t.getCapaciteMax(), distTournee));
				
				// Parcours détaillé
				StringBuilder parcours = new StringBuilder("  Parcours : 0");
				for (Client c : t.getClients())
				{
					parcours.append(" → ").append(c.number);
				}
				parcours.append(" → 0");
				logMessage(parcours.toString());
			}
			logMessage("─────────────────────────────────────");
			
			JOptionPane.showMessageDialog(InterfaceRecuitSimule.this,
				String.format("Optimisation terminée !\n\nDistance : %.2f\nVéhicules : %d\nTemps : %.2f s",
					optimisee.calculerCoutTotal(), optimisee.getNombreVehicules(), tempsTotal),
				"Résultat", JOptionPane.INFORMATION_MESSAGE);
		});
	}
	
	/**
	 * Notifie que l'optimisation a été arrêtée
	 */
	public void notifierArret()
	{
		SwingUtilities.invokeLater(() -> {
			lblStatut.setText("🛑 Arrêté");
		});
	}
	
	/**
	 * Affiche un message d'erreur
	 */
	public void afficherErreur(String message)
	{
		SwingUtilities.invokeLater(() -> {
			JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
		});
	}
	
	/**
	 * Ajoute un message au journal d'exécution
	 */
	public void logMessage(String message)
	{
		SwingUtilities.invokeLater(() -> {
			txtLog.append(message + "\n");
			txtLog.setCaretPosition(txtLog.getDocument().getLength());
		});
	}
	
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(() -> {
			try
			{
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			catch (Exception e) { }
			
			InterfaceRecuitSimule frame = new InterfaceRecuitSimule();
			frame.setVisible(true);
		});
	}
}
