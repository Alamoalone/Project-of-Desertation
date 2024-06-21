package org.SAE.Depot;

import org.SAE.Adresse.Adresse;
import org.SAE.Main.Base;
import org.SAE.Main.Logger;
import org.SAE.Main.Main;
import org.SAE.Main.SQL;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class represents a Depot, a JPanel.
 * It contains information about the Depot and methods for database operations.
 */
public class Depot extends Base {
	static final String TABLE_NAME = "Depot";
	static final String[] fields = {"Nom", "Telephone", "Presentation",
					"Commentaire", "Mail", "Website"};
	static final String[] dbFields = {"nom", "telephone", "presentation",
					"commentaire", "mail", "website"};
	static final ArrayList<String> requiredFieldsList = new ArrayList<>(Arrays.asList("Adresse id", "Referent.Referent id",
					"Nom", "Telephone"));
	public int id;
	public Adresse adresse;
	int referentIdReferent;
	boolean isArchived;
	String nom;
	String telephone;
	String presentation;
	String commentaire;
	String mail;
	String website;
	File image;
	public JourSemaine[] jourLivraison;
	public static final ArrayList<Depot> depots = new ArrayList<>();

  /**
  * Constructor for the Depot class.
  * It initializes the Depot object and adds it to the depots list.
  */
	public Depot(int id, Adresse adress, int referentIdReferent, String nom, String telephone, String presentation,
	             String commentaire, String mail, String website, File image) {
		this.id = id;
		this.adresse = adress;
		this.referentIdReferent = referentIdReferent;
		this.nom = nom;
		this.telephone = telephone;
		this.presentation = presentation;
		this.commentaire = commentaire;
		this.mail = mail;
		this.website = website;
		this.isArchived = false;
		this.image = image;
		depots.add(this);
	}

 /**
  * This method fetches the Depot data from the database and creates Depot objects.
  */
	public Depot(Adresse adresse, int referentIdReferent, String nom, String telephone, String presentation,
	             String commentaire, String mail, String website, File image) {
		this.id = Main.sql.getNextId(TABLE_NAME);
		this.adresse = adresse;
		this.referentIdReferent = referentIdReferent;
		this.nom = nom;
		this.telephone = telephone;
		this.presentation = presentation;
		this.commentaire = commentaire;
		this.mail = mail;
		this.website = website;
		this.isArchived = false;
		this.image = image;
		depots.add(this);
	}

 /**
  * This method fetches the Depot data from the database and creates Depot objects.
  */
	public static void getFromDatabase() {
		depots.clear();
		SQL sql = Main.sql;
		try {
			ResultSet res = sql.select(TABLE_NAME);
			while (res.next()) {
				int id = res.getInt("idDepot");
				int addressId = res.getInt("Adresse_idAdresse");
				int referentId = res.getInt("Referent_idReferent");
				String name = res.getString("nom");
				String telephone = res.getString("telephone");
				String presentation = res.getString("presentation");
				String comment = res.getString("commentaire");
				String mail = res.getString("mail");
				String website = res.getString("website");
				InputStream imageStream = res.getBinaryStream("image");
				boolean isArchived = res.getBoolean("estArchive");
				File image = Main.convertInputStreamToImage(imageStream);
				Depot d = new Depot(id, Adresse.getAdresseById(addressId), referentId, name, telephone, presentation, comment, mail,
								website, image);
				d.isArchived = isArchived;
				String query = "SELECT * FROM JourSemaine JOIN JourOuvrable ON JourSemaine.idJourSemaine = JourOuvrable.JourSemaine_idJourSemaine JOIN Depot ON Depot.idDepot = JourOuvrable.Depot_idDepot WHERE Depot.idDepot = " + d.id + ";";
				ResultSet res2 = sql.selectRaw(query);
				ArrayList<JourSemaine> joursLivraisons = new ArrayList<>();
				while (res2.next()) joursLivraisons.add(JourSemaine.valueOf(res2.getString("nom")));
				d.jourLivraison = joursLivraisons.toArray(new JourSemaine[0]);
			}

		} catch (Exception e) {
			Logger.error("Main.SQL Exception : " + e.getMessage());
		}

	}


  /**
  * This method updates the Depot data in the database.
  * @param depot The Depot object to be updated.
  */
	static void update(Depot depot) {
		// update jourLivraison
		if (!Main.sql.deletePrepareStatement("JourOuvrable", new String[]{"Depot_idDepot = " + depot.id}))
			Logger.error("Failed to update depot : deletion delivery days error");
		if(!Main.sql.updatePreparedStatement(TABLE_NAME, new String[]{
										"Adresse_idAdresse", "Referent_idReferent", "nom", "telephone", "presentation",
										"commentaire", "mail", "website","image", "estArchive"},
						new Object[]{depot.adresse.id, depot.referentIdReferent, depot.nom, depot.telephone,
										depot.presentation, depot.commentaire, depot.mail, depot.website,depot.image, depot.isArchived ? 1 : 0},
						new String[]{"idDepot = " + depot.id})) {
			Logger.error("Failed to update depot");
		}
		insertDeliveryDays(depot);
		getFromDatabase();

	}

	private static void insertDeliveryDays(Depot depot) {
		if (depot.jourLivraison == null) return;
		for (JourSemaine jour : depot.jourLivraison) {
			if (!Main.sql.createPrepareStatement("JourOuvrable", new String[]{"Depot_idDepot", "JourSemaine_idJourSemaine"},
							new Object[]{depot.id, jour.ordinal()+1}))
				Logger.error("Failed to update depot : creation delivery days error");
		}
	}
  /**
   * This method creates a new Depot in the database.
   * @param depot The Depot object to be created.
   */
	static void create(Depot depot) {
	if (!Main.sql.createPrepareStatement(TABLE_NAME, new String[]{"idDepot",
									"Adresse_idAdresse", "Referent_idReferent", "nom", "telephone", "presentation",
									"commentaire", "mail", "website", "image"},
					new Object[]{depot.id, depot.adresse.id, depot.referentIdReferent, depot.nom, depot.telephone,
									depot.presentation, depot.commentaire, depot.mail, depot.website, depot.image})) {
			Logger.error("Can't create depot");
			return;
		}
		insertDeliveryDays(depot);
		getFromDatabase();
	}

	public static Depot getDepotById(int depotId) {
		for (Depot depot : depots) {
			if (depot.id == depotId) return depot;
		}
		return null;
	}

	/**
  * This method deletes a Depot from the database.
  */
	protected void delete() {
		Main.sql.deletePrepareStatement("JourOuvrable", new String[]{"Depot_idDepot = " + this.id});
		if (!Main.sql.deletePrepareStatement(TABLE_NAME, new String[]{"idDepot = " + this.id})) {
			Logger.error("Can't delete depot");
			return;
		}
		depots.remove(this);
	}

 /**
  * This method archives a Depot.
  */
	void archive() {
		if (!Main.sql.updatePreparedStatement(TABLE_NAME, new String[]{"estArchive"}, new Object[]{this.isArchived ? 0 : 1},
						new String[]{"idDepot = " + this.id}))
			Logger.error("Can't archive depot");
		this.isArchived = !this.isArchived;
	}
	@Override
	public void loadFromDatabase(){
		getFromDatabase();
	}

	@Override
	public String toString() {
		return nom;
	}
}
