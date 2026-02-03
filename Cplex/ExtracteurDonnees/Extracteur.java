import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class Extracteur {
	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Usage: java Extracteur <input_file>");
			return;
		}

		String texte = "";
		try
		{
			boolean canCopy = false;
			Scanner sc = new Scanner ( new FileInputStream ( args[0] ), "UTF8" );

			String ligne;
			while (sc.hasNextLine())
			{
				ligne = sc.nextLine();

				if (ligne.isEmpty()) {break;}

				if (canCopy) {texte += ligne + "\n";}

				if (ligne.contains("0 0")) {canCopy = true;}
			}

			sc.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		extractData(texte);
	}

	public static void extractData(String text) {
		ArrayList<Client> lstClient = new ArrayList<>();
		
		String[] lines = text.split("\n");
		for (String line : lines) {
			line = line.trim();
			String[] data = line.split(" ");

			try {
				if (data.length >= 3) {
					int number = Integer.parseInt(data[0]);
					int demand = Integer.parseInt(data[2]);
					lstClient.add(new Client(number, demand));
				}
			} catch (NumberFormatException e) {	e.printStackTrace(); }
		}
	}

	public static void writeReadableData(ArrayList<Client> lstClient, double capaciteVehicule, int nbVehicules)
}