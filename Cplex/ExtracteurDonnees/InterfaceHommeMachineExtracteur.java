import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class InterfaceHommeMachineExtracteur extends JFrame {
	private JButton btnGenerer;
	private JButton btnSelectionnerFichier;
	private JButton btnSelectionnerDossier;
	private JTextField inputNbVehicules;
	private JTextField inputNomFichier;

	private String cheminFichierSource;
	private String cheminDossierDestination;


	public InterfaceHommeMachineExtracteur() {
		setTitle("Extracteur Donnees");
		setSize(400, 300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
		setLayout( new BoxLayout(getContentPane(), BoxLayout.Y_AXIS) );

		 initializeComponents();
		 addComponentsToFrame();
	}

	private void addComponentsToFrame() {
		add(btnGenerer);
		add(btnSelectionnerFichier);
		add(btnSelectionnerDossier);
		add(inputNbVehicules);
		add(inputNomFichier);
	}

	private void initializeComponents() {
		btnGenerer = new JButton("Générer Données");
		btnSelectionnerFichier = new JButton("Sélectionner Fichier");
		btnSelectionnerDossier = new JButton("Sélectionner Dossier");
		inputNbVehicules = new JTextField("Nombre de Véhicules");
		inputNomFichier  = new JTextField("Nom du Fichier");

		btnGenerer.setAlignmentX(CENTER_ALIGNMENT);
		btnSelectionnerFichier.setAlignmentX(CENTER_ALIGNMENT);
		btnSelectionnerDossier.setAlignmentX(CENTER_ALIGNMENT);
		inputNbVehicules.setMaximumSize( new java.awt.Dimension(200, 30) );
		inputNbVehicules.setAlignmentX(CENTER_ALIGNMENT);
		inputNomFichier.setMaximumSize( new java.awt.Dimension(200, 30) );
		inputNomFichier.setAlignmentX(CENTER_ALIGNMENT);

		btnGenerer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String[] args = new String[]{
						cheminFichierSource,
						inputNbVehicules.getText(),
						cheminDossierDestination+"/"+inputNomFichier.getText()+".dat"
				};
				Extracteur.main(args);
			}
		});

		btnSelectionnerFichier.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int result = fileChooser.showOpenDialog(this);
				if (result == JFileChooser.APPROVE_OPTION) {
					cheminFichierSource = fileChooser.getSelectedFile().getAbsolutePath();
				}
			}
		});

		btnSelectionnerDossier.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser dirChooser = new JFileChooser();
				dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int result = dirChooser.showOpenDialog(this);
				if (result == JFileChooser.APPROVE_OPTION) {
					cheminDossierDestination = dirChooser.getSelectedFile().getAbsolutePath();
				}
			}
		});
	}

	
}