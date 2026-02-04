import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class Extracteur {
	public static void main(String[] args) {

		if (args.length != 3) {
			System.out.println("Usage: java Extracteur <fichier_importé> <nombre_vehicules> <chemin_ecriture>");
			return;
		}

		String cheminEcriture = args[2];

		int nbVehicules = Integer.parseInt(args[1]);
		int capaciteVehicule = 0;


		String texte = "";
		// Extraction de la capacité des véhicules
		try	{
			Scanner sc = new Scanner ( new FileInputStream ( args[0] ), "UTF8" );

			String ligne = "";
			for(int i = 0; i < 2; i++) { ligne = sc.nextLine(); }
			capaciteVehicule = Integer.parseInt(ligne.trim());

			sc.close();
		} 
		catch (Exception e)	{
			e.printStackTrace();
		}

		// Extraction des clients
		texte = "";
		try	{
			boolean canCopy = false;
			Scanner sc = new Scanner ( new FileInputStream ( args[0] ), "UTF8" );

			String ligne;
			while (sc.hasNextLine())
			{
				ligne = sc.nextLine();

				if (ligne.isEmpty()) {break;}
				if (ligne.replaceAll("\\s+", " ").split(" ").length >= 4) {canCopy = true;}
			
				if (canCopy) {texte += ligne + "\n";}

			}

			sc.close();
		}
		catch (Exception e)	{
			e.printStackTrace();
		}

		ArrayList<Client> lstClient = extractData(texte);
		writeReadableData(lstClient, capaciteVehicule, nbVehicules, cheminEcriture);
	}

	public static ArrayList<Client> extractData(String text) {
		ArrayList<Client> lstClient = new ArrayList<>();

		lstClient.add(new Client(0, 0, 0, 0)); // Ajout du dépôt
		
		String[] lines = text.split("\n");
		for (String line : lines) {
			line = line.replaceAll("\\s+", " ");
			String[] data = line.split(" ");

			try {
				if (data.length >= 4) {
					int number = Integer.parseInt(data[1]);
					int demand = Integer.parseInt(data[4]);
					int x = Integer.parseInt(data[2]);
					int y = Integer.parseInt(data[3]);
					lstClient.add(new Client(number, demand, x, y));
				}
			} catch (NumberFormatException e) {	}//e.printStackTrace(); }
		}

		return lstClient;
	}

	public static void writeReadableData(ArrayList<Client> lstClient, int capaciteVehicule, int nbVehicules, String cheminEcriture) {
		java.io.File file = new java.io.File(cheminEcriture);
		if (file.exists()) {
			file.delete();
		}

		try {
			java.io.FileWriter writer = new java.io.FileWriter(cheminEcriture, true);

			writer.write("/*********************************************\n" + //
						 " * OPL 22.1.1.0 Data\n" + //
						 " * Author: oj230564\n" + //
						 " * Creation Date: 2 fevr. 2026 at 09:33:55\n" + //
						 " *********************************************/\n\n");
			
			String noeuds = "noeuds={";
			String demandes = "demande=[";
			String distances = "distances=[\n";

			for (Client client : lstClient) {
				noeuds += ",\"" + client.number() + "\"";
				if (client.number() != 0) {
					demandes += client.demand();
					if (client != lstClient.get(lstClient.size() - 1)) {
						demandes += ", ";
					}
				}

				distances += "[";
				
				for (Client otherClient : lstClient) {
					double distance = Math.sqrt(Math.pow(client.x() - otherClient.x(), 2) + Math.pow(client.y() - otherClient.y(), 2));
					distances += String.format(java.util.Locale.US, "%.2f", distance);
					if (otherClient != lstClient.get(lstClient.size() - 1)) {
						distances += ", ";
					}
				}

				
				distances += "]";
				if (client != lstClient.get(lstClient.size() - 1)) {
					distances += ",\n";
				}
			}

			writer.write(noeuds + "};\ndepot=\"0\";\n");
			writer.write("nbvehicules=" + nbVehicules + ";\n");
			writer.write("Qmax=" + capaciteVehicule + ";\n");
			writer.write("NbClients=" + (lstClient.size()-1) + ";\n\n\n");

			writer.write(demandes + "];\n\n");
			writer.write(distances + "\n];");

			writer.close();

			System.out.println("Data written successfully to " + cheminEcriture);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}