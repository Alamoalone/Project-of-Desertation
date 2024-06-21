package org.SAE.Adresse;

import org.SAE.Client.Client;
import org.SAE.Main.Base;
import org.SAE.Main.Main;
import org.SAE.Main.SQL;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class represents an address with properties like ID, address, city, and postal code.
 * It provides methods to create, update, delete, and retrieve addresses from the database.
 */
public class Adresse extends Base {
	public static final String TABLE_NAME = "Adresse";
	protected static final String[] TABLE_COLUMNS = {"adresse", "ville", "codePostal"};
	public int id;
	String rue;
	String ville;
	String codePostal;

	// A static list to hold all the addresses
	public static final List<Adresse> adresses = new ArrayList<>();

	/**
	 * Constructor to create an address object with an ID, address, city, and postal code.
	 * The address object is then added to the static list of addresses.
	 */
	Adresse(int id, String rue, String ville, String codePostal) {
		this.id = id;
		this.rue = rue;
		this.ville = ville;
		this.codePostal = codePostal;
		adresses.add(this);
	}

	/**
	 * Constructor to create an address object with address, city, and postal code.
	 * The ID is automatically set to the current size of the static list of addresses.
	 * The address object is then added to the static list of addresses.
	 */
	public Adresse(String rue, String ville, String codePostal) {
		this.rue = rue;
		this.ville = ville;
		this.codePostal = codePostal;
		adresses.add(this);
	}

	/**
	 * Method to update an address in the database.
	 * If the update fails, it prints “Update failed” to the console.
	 */
	static void update(Adresse adresse) {
		if (!(Main.sql.updatePreparedStatement(TABLE_NAME, TABLE_COLUMNS,
						new Object[]{adresse.rue, adresse.ville, adresse.codePostal},
						new String[]{"idAdresse = " + adresse.id}))) org.SAE.Main.Logger.log("Update failed");
		getFromDatabase();

	}

	public static Adresse getAdresseById(int adresseIdAdresse) {
		for (Adresse adresse : adresses) if (adresse.id == adresseIdAdresse) return adresse;
		return null;
	}

	/**
	 * Method to delete an address from the database.
	 * If the deletion fails, it prints “Delete failed” to the console.
	 */
	@Override
	protected void delete() {
		// delete linked clients
		for (int i = 0; i < Client.clients.size(); i++) {
			Client client = Client.clients.get(i);
			if (client.adresse == this) client.delete();
		}
		// delete address
		if (!Main.sql.deletePrepareStatement(TABLE_NAME, new String[]{"idAdresse = " + id}))
			org.SAE.Main.Logger.log("Delete failed");
		adresses.remove(this);
	}

	/**
	 * Method to return a string representation of the address object.
	 */
	public String toString() {
		return rue + ", " + ville + ", " + codePostal;
	}

	/**
	 * Method to retrieve all addresses from the database and add them to the static list of addresses.
	 */
	public static void getFromDatabase() {
		SQL sql = Main.sql;
		adresses.clear();
		try {
			ResultSet res = sql.select(TABLE_NAME);
			if (res == null) return;
			while (res.next()) {
				int id = res.getInt("idAdresse");
				String adresse = res.getString("adresse");
				String ville = res.getString("ville");
				String codePostal = res.getString("codePostal");
				new Adresse(id, adresse, ville, codePostal);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Method to create an address in the database.
	 * If the creation fails, it prints “Create failed” to the console.
	 */
	static void create(Adresse adresse) {
		String[] tableColumns = {"idAdresse"};
		for (String column : TABLE_COLUMNS) tableColumns = Main.addStringToArray(tableColumns, column);
		if (!Main.sql.createPrepareStatement(TABLE_NAME, tableColumns,
						new Object[]{adresse.id, adresse.rue, adresse.ville, adresse.codePostal}))
			org.SAE.Main.Logger.log("Create failed");
		getFromDatabase();
	}

	@Override
	public void loadFromDatabase() {
		getFromDatabase();
	}

}
