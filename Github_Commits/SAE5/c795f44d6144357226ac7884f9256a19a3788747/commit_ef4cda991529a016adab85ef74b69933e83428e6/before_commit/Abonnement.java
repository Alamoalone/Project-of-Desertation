package org.SAE.Abonnement;

import org.SAE.Client.Client;
import org.SAE.Livraison.Livraison;
import org.SAE.Main.Base;
import org.SAE.Main.Logger;
import org.SAE.Main.Main;
import org.SAE.Main.SQL;
import org.SAE.Panier.Panier;

import java.sql.Date;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a Depot, a JPanel.
 * It contains information about the Depot and methods for database operations.
 */
public class Abonnement extends Base {
	static final String TABLE_NAME = "Abonnement";
	static final String[] dbFields = {"Client_idClient", "Panier_idPanier", "debut", "fin", "estActif", "frequenceLivraison"};

	public int id;
	Client client;
	Panier panier;
	boolean estActif;
	Date debut;
	Date fin;
	int frequenceLivraison;
	static final List<Abonnement> abonnements = new ArrayList<>();

	Abonnement(Client client, Panier panier, Date debut, Date fin,
	           int frequenceLivraison,
	           boolean estActif) {
		this.client = client;
		this.panier = panier;
		this.debut = debut;
		this.fin = fin;
		this.frequenceLivraison = frequenceLivraison;
		this.estActif = estActif;
		abonnements.add(this);
	}

	Abonnement(int id, Client client, Panier panier, Date debut, Date fin,
	           int frequenceLivraison,
	           boolean estActif) {
		this.id = id;
		this.client = client;
		this.panier = panier;
		this.debut = debut;
		this.fin = fin;
		this.frequenceLivraison = frequenceLivraison;
		this.estActif = estActif;
		abonnements.add(this);
	}


	/**
	 * This method fetches the Abonnement data from the database and creates Depot objects.
	 */
	public static void getFromDatabase() {
		abonnements.clear();
		SQL sql = Main.sql;
		try {
			ResultSet res = sql.select(TABLE_NAME);
			while (res.next()) {
				int id = res.getInt("idAbonnement");
				int clientIdClient = res.getInt("Client_idClient");
				int panierIdPanier = res.getInt("Panier_idPanier");
				Date debut = res.getDate("debut");
				Date fin = res.getDate("fin");
				int frequenceLivraison = res.getInt("frequenceLivraison");
				boolean estActif = res.getBoolean("estActif");
				// if datefin < today, estActif = false
				if (fin != null && fin.before(new Date(System.currentTimeMillis()))) estActif = false;
				// update database
				if (!sql.updatePreparedStatement(TABLE_NAME, new String[]{"estActif"},
								new Object[]{estActif},
								new String[]{"idAbonnement = " + id}))
					Logger.error("Can't update abonnement");
				Client client = Client.clients.stream().filter(c -> c.id == clientIdClient).findFirst().orElse(null);
				Panier panier = Panier.paniers.stream().filter(p -> p.id == panierIdPanier).findFirst().orElse(null);
				if (client == null || panier == null) {
					Logger.error("Error while fetching Abonnement data from database: Client or Panier not found");
					return;
				}
				new Abonnement(id, client, panier, debut, fin, frequenceLivraison, estActif);
			}

		} catch (Exception e) {
			Logger.error("Main.SQL Exception : " + e.getMessage());
		}

	}

	public static Abonnement getAbonnementById(int abonnementId) {
		return abonnements.stream().filter(a -> a.id == abonnementId).findFirst().orElse(null);
	}

	@Override
	public void loadFromDatabase() {
		getFromDatabase();
	}

	/**
	 * This method updates the abonnement in the database.
	 *
	 * @param abonnement The Abonnement object to be updated.
	 */
	static void update(Abonnement abonnement) {
		if (!Main.sql.updatePreparedStatement(TABLE_NAME, dbFields,
						new Object[]{abonnement.client.id, abonnement.panier.id, abonnement.debut, abonnement.fin, abonnement.estActif,
										abonnement.frequenceLivraison},
						new String[]{"idAbonnement = " + abonnement.id}))
			Logger.error("Can't update abonnement");
		getFromDatabase();
		abonnement.updateLivraison();
	}

	/**
	 * This method creates a new Abonnement in the database.
	 *
	 * @param abonnement The Abonnement object to be created.
	 */
	static void create(Abonnement abonnement) {
		if (!Main.sql.createPrepareStatement(TABLE_NAME, dbFields,
						new Object[]{abonnement.client.id, abonnement.panier.id, abonnement.debut, abonnement.fin, abonnement.estActif,
										abonnement.frequenceLivraison}))
			Logger.error("Can't create abonnement");
		getFromDatabase();
		abonnement.updateLivraison();
	}

	void updateLivraison() {
		long frequence = (long) frequenceLivraison * 24 * 60 * 60 * 1000;
		Date[] datesLivraison = new Date[(int) ((fin.getTime()-debut.getTime()) / frequence + 1)];
		Date date = debut;
		System.out.println((fin.getTime()-debut.getTime()) / frequence);
		for (int i = 0; i < datesLivraison.length; i++) {
			datesLivraison[i] = date;
			date = new Date(date.getTime() + (long) frequenceLivraison * 24 * 60 * 60 * 1000);
			System.out.println(date);
		}
		// delete old livraisons
		for (Livraison livraison : Livraison.livraisons) {
			if (livraison.abonnement == this &&
							(livraison.date.before(new Date(System.currentTimeMillis())) &&
											(!livraison.etat.equals("livre") && (!livraison.etat.equals("en cours")))))
				livraison.delete();
		}
		for (Date dateLivraison : datesLivraison) {
			if (dateLivraison.before(new Date(System.currentTimeMillis()))) continue;
			Livraison livraison = new Livraison(null, null, this, dateLivraison, "En attente");
			Livraison.create(livraison);
		}
	}

	/**
	 * This method deletes the Abonnement from the database.
	 */
	public void delete() {
		if (!Main.sql.deletePrepareStatement(TABLE_NAME, new String[]{"idAbonnement = " + this.id}))
			Logger.error("Can't delete abonnement");
		abonnements.remove(this);
	}


	@Override
	public String toString() {
		return "Pour " + client + " : " + panier + " du " + debut + " au " + fin + " tous les " + frequenceLivraison + " jours";
	}
}
