import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;


public class InterfaceHommeMachineExtracteur extends JFrame {
	private JButton btnGenerer;
	private JButton btnSelectionnerFichier;
	private JButton btnSelectionnerDossier;
	private JTextField inputNbVehicules;
	private JTextField inputNomFichier;
	private JLabel lblFichierSelectionne;
	private JLabel lblDossierSelectionne;
	private JLabel lblStatus;

	private String cheminFichierSource;
	private String cheminDossierDestination;


	public InterfaceHommeMachineExtracteur() {
		// Utiliser le Look and Feel natif du système
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// Ignorer si échec, utiliser le look par défaut
		}
		
		setTitle("Extracteur de Données VRP");
		setSize(650, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		
		initializeComponents();
		addComponentsToFrame();
		
		setVisible(true);
	}

	private void addComponentsToFrame() {
		setLayout(new BorderLayout(10, 10));
		
		// Panel principal avec marge
		JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
		mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		
		// Titre
		JLabel titre = new JLabel("Extracteur de Données VRP", JLabel.CENTER);
		titre.setFont(new Font("Arial", Font.BOLD, 20));
		titre.setBorder(new EmptyBorder(0, 0, 15, 0));
		mainPanel.add(titre, BorderLayout.NORTH);
		
		// Panel central avec les contrôles
		JPanel centrePanel = new JPanel(new GridBagLayout());
		centrePanel.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createLineBorder(Color.GRAY, 1),
			"Configuration",
			TitledBorder.LEFT,
			TitledBorder.TOP,
			new Font("Arial", Font.BOLD, 14)
		));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(8, 10, 8, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		// Fichier source
		gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 0;
		JLabel lblFichierSource = new JLabel("Fichier source :");
		lblFichierSource.setFont(new Font("Arial", Font.PLAIN, 13));
		centrePanel.add(lblFichierSource, gbc);
		
		gbc.gridx = 1; gbc.weightx = 1.0;
		centrePanel.add(btnSelectionnerFichier, gbc);
		
		gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
		centrePanel.add(lblFichierSelectionne, gbc);
		
		// Séparateur
		gbc.gridy = 2; gbc.insets = new Insets(15, 10, 8, 10);
		centrePanel.add(new JSeparator(), gbc);
		
		// Dossier destination
		gbc.gridy = 3; gbc.gridwidth = 1; gbc.weightx = 0; gbc.insets = new Insets(8, 10, 8, 10);
		JLabel lblDossierDest = new JLabel("Dossier destination :");
		lblDossierDest.setFont(new Font("Arial", Font.PLAIN, 13));
		centrePanel.add(lblDossierDest, gbc);
		
		gbc.gridx = 1; gbc.weightx = 1.0;
		centrePanel.add(btnSelectionnerDossier, gbc);
		
		gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
		centrePanel.add(lblDossierSelectionne, gbc);
		
		// Séparateur
		gbc.gridy = 5; gbc.insets = new Insets(15, 10, 8, 10);
		centrePanel.add(new JSeparator(), gbc);
		
		// Nombre de véhicules
		gbc.gridy = 6; gbc.gridwidth = 1; gbc.weightx = 0; gbc.insets = new Insets(8, 10, 8, 10);
		JLabel lblNbVehicules = new JLabel("Nombre de véhicules :");
		lblNbVehicules.setFont(new Font("Arial", Font.PLAIN, 13));
		centrePanel.add(lblNbVehicules, gbc);
		
		gbc.gridx = 1; gbc.weightx = 1.0;
		centrePanel.add(inputNbVehicules, gbc);
		
		// Nom du fichier
		gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0;
		JLabel lblNomFichier = new JLabel("Nom du fichier :");
		lblNomFichier.setFont(new Font("Arial", Font.PLAIN, 13));
		centrePanel.add(lblNomFichier, gbc);
		
		gbc.gridx = 1; gbc.weightx = 1.0;
		centrePanel.add(inputNomFichier, gbc);
		
		mainPanel.add(centrePanel, BorderLayout.CENTER);
		
		// Panel du bas avec bouton et status
		JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
		bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
		
		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		btnPanel.add(btnGenerer);
		bottomPanel.add(btnPanel, BorderLayout.NORTH);
		
		bottomPanel.add(lblStatus, BorderLayout.SOUTH);
		
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);
		
		add(mainPanel);
	}

	private void initializeComponents() {
		// Boutons
		btnGenerer             = new JButton("⚙ Générer les Données");
		btnSelectionnerFichier = new JButton("📁 Parcourir...");
		btnSelectionnerDossier = new JButton("📁 Parcourir...");
		
		// Style des boutons
		btnGenerer.setFont(new Font("Arial", Font.BOLD, 14));
		btnGenerer.setPreferredSize(new Dimension(200, 40));
		btnGenerer.setBackground(new Color(70, 130, 180));
		btnGenerer.setForeground(Color.WHITE);
		btnGenerer.setFocusPainted(false);
		btnGenerer.setCursor(new Cursor(Cursor.HAND_CURSOR));
		
		btnSelectionnerFichier.setPreferredSize(new Dimension(150, 30));
		btnSelectionnerFichier.setCursor(new Cursor(Cursor.HAND_CURSOR));
		
		btnSelectionnerDossier.setPreferredSize(new Dimension(150, 30));
		btnSelectionnerDossier.setCursor(new Cursor(Cursor.HAND_CURSOR));
		
		// Champs de texte
		inputNbVehicules = new JTextField(10);
		inputNomFichier  = new JTextField(20);
		
		inputNbVehicules.setFont(new Font("Arial", Font.PLAIN, 13));
		inputNomFichier.setFont(new Font("Arial", Font.PLAIN, 13));
		
		// Labels de status
		lblFichierSelectionne = new JLabel("Aucun fichier sélectionné");
		lblFichierSelectionne.setFont(new Font("Arial", Font.ITALIC, 11));
		lblFichierSelectionne.setForeground(Color.GRAY);
		
		lblDossierSelectionne = new JLabel("Aucun dossier sélectionné");
		lblDossierSelectionne.setFont(new Font("Arial", Font.ITALIC, 11));
		lblDossierSelectionne.setForeground(Color.GRAY);
		
		lblStatus = new JLabel(" ");
		lblStatus.setFont(new Font("Arial", Font.PLAIN, 12));
		lblStatus.setHorizontalAlignment(JLabel.CENTER);
		lblStatus.setBorder(new EmptyBorder(5, 0, 0, 0));

		btnGenerer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Validation des entrées
				if (cheminFichierSource == null || cheminFichierSource.isEmpty()) {
					afficherErreur("Veuillez sélectionner un fichier source.");
					return;
				}
				if (cheminDossierDestination == null || cheminDossierDestination.isEmpty()) {
					afficherErreur("Veuillez sélectionner un dossier de destination.");
					return;
				}
				if (inputNbVehicules.getText().trim().isEmpty()) {
					afficherErreur("Veuillez entrer le nombre de véhicules.");
					return;
				}
				if (inputNomFichier.getText().trim().isEmpty()) {
					afficherErreur("Veuillez entrer un nom de fichier.");
					return;
				}
				
				// Validation du nombre de véhicules
				try {
					int nbVehicules = Integer.parseInt(inputNbVehicules.getText().trim());
					if (nbVehicules <= 0) {
						afficherErreur("Le nombre de véhicules doit être positif.");
						return;
					}
				} catch (NumberFormatException ex) {
					afficherErreur("Le nombre de véhicules doit être un nombre entier valide.");
					return;
				}
				
				try {
					lblStatus.setText("⏳ Génération en cours...");
					lblStatus.setForeground(new Color(255, 140, 0));
					
					String[] args = new String[]{
							cheminFichierSource,
							inputNbVehicules.getText().trim(),
							cheminDossierDestination + "/" + inputNomFichier.getText().trim() + ".dat"
					};
					Extracteur.main(args);
					
					lblStatus.setText("✓ Données générées avec succès !");
					lblStatus.setForeground(new Color(34, 139, 34));
					
					JOptionPane.showMessageDialog(
						InterfaceHommeMachineExtracteur.this,
						"Les données ont été générées avec succès dans :\n" + 
						cheminDossierDestination + "/" + inputNomFichier.getText().trim() + ".dat",
						"Succès",
						JOptionPane.INFORMATION_MESSAGE
					);
				} catch (Exception ex) {
					lblStatus.setText("✗ Erreur lors de la génération");
					lblStatus.setForeground(Color.RED);
					JOptionPane.showMessageDialog(
						InterfaceHommeMachineExtracteur.this,
						"Erreur lors de la génération : " + ex.getMessage(),
						"Erreur",
						JOptionPane.ERROR_MESSAGE
					);
				}
			}
		});

		btnSelectionnerFichier.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Utiliser FileDialog pour dialogue natif de l'OS
				FileDialog fileDialog = new FileDialog(InterfaceHommeMachineExtracteur.this, 
					"Sélectionner le fichier source", FileDialog.LOAD);
				
				// Définir le répertoire initial si existant
				if (cheminFichierSource != null) {
					File parent = new File(cheminFichierSource).getParentFile();
					if (parent != null && parent.exists()) {
						fileDialog.setDirectory(parent.getAbsolutePath());
					}
				}
				
				// Afficher le dialogue (bloquant jusqu'à sélection)
				fileDialog.setVisible(true);
				
				// Récupérer le fichier sélectionné
				String fileName = fileDialog.getFile();
				String dirName = fileDialog.getDirectory();
				
				if (fileName != null && dirName != null) {
					File selectedFile = new File(dirName, fileName);
					cheminFichierSource = selectedFile.getAbsolutePath();
					lblFichierSelectionne.setText("✓ " + selectedFile.getName());
					lblFichierSelectionne.setForeground(new Color(34, 139, 34));
					lblFichierSelectionne.setToolTipText(cheminFichierSource);
					lblStatus.setText(" ");
				}
			}
		});

		btnSelectionnerDossier.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// JFileChooser pour sélection de dossiers (FileDialog ne supporte pas bien les dossiers sur Linux)
				JFileChooser dirChooser = new JFileChooser();
				dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				dirChooser.setDialogTitle("Sélectionner le dossier de destination");
				dirChooser.setApproveButtonText("Sélectionner");
				
				// Définir le répertoire initial si existant
				if (cheminDossierDestination != null) {
					File dir = new File(cheminDossierDestination);
					if (dir.exists() && dir.isDirectory()) {
						dirChooser.setCurrentDirectory(dir);
						dirChooser.setSelectedFile(dir);
					}
				}
				
				int result = dirChooser.showDialog(InterfaceHommeMachineExtracteur.this, null);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedDir = dirChooser.getSelectedFile();
					if (selectedDir != null && selectedDir.isDirectory()) {
						cheminDossierDestination = selectedDir.getAbsolutePath();
						lblDossierSelectionne.setText("✓ " + selectedDir.getName());
						lblDossierSelectionne.setForeground(new Color(34, 139, 34));
						lblDossierSelectionne.setToolTipText(cheminDossierDestination);
						lblStatus.setText(" ");
					}
				}
			}
		});
	}
	
	private void afficherErreur(String message) {
		lblStatus.setText("✗ " + message);
		lblStatus.setForeground(Color.RED);
		JOptionPane.showMessageDialog(
			this,
			message,
			"Erreur de validation",
			JOptionPane.WARNING_MESSAGE
		);
	}

	public static void main(String[] args) {
		new InterfaceHommeMachineExtracteur();
	}
}
