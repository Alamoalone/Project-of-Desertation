package at.bfi.projekt.firma_v3;

class Verwaltung {

	public static double summeAllerGehaelter;
	public static final double TAX_ANG = 0.20;
	public static final double TAX_ARB = 0.15;

	// Array fuer Abteilungsliste
	public static Abteilung[] abt_liste = new Abteilung[4];

	public static void main(String[] args) {

		final int SIZE = 12;

		// Leeres Array fuer die Mitarbeiterliste
		Mitarbeiter[] mitarbeiter_liste = new Mitarbeiter[SIZE];

		// Erstellung der Arbeiterobjekte
		Arbeiter arbeiter_0 = new Arbeiter(1, "Anneken", 29.1, 109, 94.4, 12.2);
		Arbeiter arbeiter_1 = new Arbeiter(2, "Poldi", 31.7, 97, 101.3, 13.3);
		Arbeiter arbeiter_2 = new Arbeiter(3, "Ottila", 17.2, 83, 44.9, 14.4);
		Arbeiter arbeiter_3 = new Arbeiter(4, "Eckehard", 27.8, 134, 104.7, 15.5);
		Arbeiter arbeiter_4 = new Arbeiter(5, "Juliane", 23.7, 124, 114.1, 16.6);
		Arbeiter arbeiter_5 = new Arbeiter(6, "Erna", 22.5, 123, 111.8, 17.7);

		// Erstellung der Angestelltenobjekte
		Angestellter angestellter_6 = new Angestellter(7, "Mathis", 2400.00, 1211.30, 18.8);
		Angestellter angestellter_7 = new Angestellter(8, "Hardwin", 2800.00, 1309.80, 19.9);
		Angestellter angestellter_8 = new Angestellter(9, "Barthold", 1812.23, 992.20, 20.0);
		Angestellter angestellter_9 = new Angestellter(10, "Ortrun", 1903.67, 1489.10, 21.1);
		Angestellter angestellter_10 = new Angestellter(11, "Loreley", 1407.95, 1298.21, 22.2);
		Angestellter angestellter_11 = new Angestellter(12, "Wilmar", 1703.67, 1496.21, 23.3);

		// Zuweisung der Objekte zum Array mit der Liste der Mitarbeiter
		mitarbeiter_liste[0] = arbeiter_0;
		mitarbeiter_liste[1] = arbeiter_1;
		mitarbeiter_liste[2] = arbeiter_2;
		mitarbeiter_liste[3] = arbeiter_3;
		mitarbeiter_liste[4] = arbeiter_4;
		mitarbeiter_liste[5] = arbeiter_5;

		mitarbeiter_liste[6] = angestellter_6;
		mitarbeiter_liste[7] = angestellter_7;
		mitarbeiter_liste[8] = angestellter_8;
		mitarbeiter_liste[9] = angestellter_9;
		mitarbeiter_liste[10] = angestellter_10;
		mitarbeiter_liste[11] = angestellter_11;

		abt_liste[0] = new Abteilung(1, "PR.", new Mitarbeiter[] { mitarbeiter_liste[0], mitarbeiter_liste[5],
				mitarbeiter_liste[6], mitarbeiter_liste[7] });
		abt_liste[1] = new Abteilung(2, "SALES", new Mitarbeiter[] { mitarbeiter_liste[1], mitarbeiter_liste[2],
				mitarbeiter_liste[8], mitarbeiter_liste[9], mitarbeiter_liste[10] });
		abt_liste[2] = new Abteilung(3, "IT",
				new Mitarbeiter[] { mitarbeiter_liste[4], mitarbeiter_liste[5], mitarbeiter_liste[11] });
		abt_liste[3] = new Abteilung(4, "HR", new Mitarbeiter[] { mitarbeiter_liste[3] });

		System.out.println("*** Alle Abteilungen:");
		ausgabe_AlleAbteilungen();
		System.out.println("*** Ende der Ausgabe aller Abteilungen\n");

		System.out.println("### Wir holen uns die Mitarbeiterliste aus der PR-Abteilung (ID 1): ");
		Mitarbeiter[] mitarbeiterPrAbteilung = getMitarbeiterListeVonAbteilung(1);
		for (Mitarbeiter prMitarbeiter : mitarbeiterPrAbteilung) {
			System.out.println(prMitarbeiter.ausgabe());
		}
		System.out.println("### Ende Ausgabe der PR Mitarbeiter\n");

		System.out.println("### NULL POINTER? Wir holen uns die Mitarbeiterliste aus der PR-Abteilung (ID 1): ");
		Mitarbeiter[] mitarbeiterPrAbteilung1 = getMitarbeiterListeVonAbteilung(6);
		for (Mitarbeiter prMitarbeiter1 : mitarbeiterPrAbteilung1) {
			System.out.println(prMitarbeiter1.ausgabe());
		}
		System.out.println("### Ende Ausgabe der PR Mitarbeiter\n");

		System.out.println("+++ Wir suchen den Mitarbeiter mit der ID 7. Es sollte Mathis sein.");
		System.out.println(searchMitarbeiterAusFirma(7).ausgabe());
		System.out.println("\n");
		System.out.println("+++ Ende Ausgabe des gesuchten Mitarbeiters\n");

//		BISHERIGE Tests sind kommentiert, um die Ausgabe der neuen Funktionen leicht auffindbar zu machen.

//		// Suche nach Mitarbeiter anhand der ID
//		Mitarbeiter testMitarbeiter1 = Mitarbeiter.suche(mitarbeiter_liste, 3);
//		Mitarbeiter testMitarbeiter2 = Mitarbeiter.suche(mitarbeiter_liste, 8);
//
//		// Ausgabe der gefundenen Mitarbeiter
//		System.out.println("\nAusgabe einzelner Mitarbeiter,\nnachdem sie ueber die ID gefunden wurden:\n");
//		Mitarbeiter.ausgabe(testMitarbeiter1);
//		Mitarbeiter.ausgabe(testMitarbeiter2);
//
//		// Ausgabe der gesamten Mitarbeiterliste
//		System.out.println("\n\nAusgabe der gesamten MitarbeiterListe");
//		System.out.println("=========================================\n");
//
//		Mitarbeiter.ausgabe(mitarbeiter_liste);
//
//		// Ausgabe der Anzahl der Mitarbeiter (zuerst Angstellte dann Arbeiter)
//		System.out.print("\nAnzahl der Angestellten: ");
//		System.out.print(Mitarbeiter.getAnzAngestellterGesamt(mitarbeiter_liste));
//
//		System.out.println("\nAnzahl der Arbeiter: " + Mitarbeiter.getAnzArbeiterGesamt(mitarbeiter_liste));
//
//		berechneSummeAlleGehaelter(mitarbeiter_liste);
//		System.out.println("\nSumme aller Gehaelter: " + summeAllerGehaelter);
//
//		System.out.print("\nNettogehalt des Arbeiters mit der ID " + mitarbeiter_liste[3].getId() + ": ");
//		System.out.printf("%.2f%n", berechneNettoGehalt(mitarbeiter_liste[3]));
//
//		System.out.print("\nNettogehalt des Angestellten mit der ID " + mitarbeiter_liste[11].getId() + ": ");
//		System.out.printf("%.2f%n", berechneNettoGehalt(mitarbeiter_liste[11]));

	}

	/**
	 * @param mitarbeiter_liste
	 */
	public static void berechneSummeAlleGehaelter(Mitarbeiter[] mitarbeiter_liste) {
		summeAllerGehaelter = 0.0;
		for (Mitarbeiter dieserMitarbeiter : mitarbeiter_liste) {
			summeAllerGehaelter += dieserMitarbeiter.berechneBrutto();
		}
	};

	/**
	 * @param dieserMitarbeiter
	 * @return
	 */
	public static double berechneNettoGehalt(Mitarbeiter dieserMitarbeiter) {
		double bruttoBetrag = dieserMitarbeiter.berechneBrutto();
		double steuerBetrag = 0;

		if (dieserMitarbeiter instanceof Arbeiter) {
			steuerBetrag = bruttoBetrag * TAX_ARB;
		}
		if (dieserMitarbeiter instanceof Angestellter) {
			steuerBetrag = bruttoBetrag * TAX_ANG;
		}

		return bruttoBetrag - steuerBetrag;
	};

	public static void ausgabe_AlleAbteilungen() {
		for (Abteilung dieseAbteilung : abt_liste) {
			System.out.println(dieseAbteilung.ausgabe());
		}
	}

	/**
	 * @param abt_id
	 * @return
	 */
	public static Mitarbeiter[] getMitarbeiterListeVonAbteilung(int abt_id) {
		for (Abteilung dieseAbteilung : abt_liste) {
			if (dieseAbteilung.getId() == abt_id) {
				return dieseAbteilung.getMitarb_liste();
			}
		}
		return new Mitarbeiter[0];
	}

	/**
	 * @param mitarbeiter_id
	 * @return
	 */
	public static Mitarbeiter searchMitarbeiterAusFirma(int mitarbeiter_id) {
		for (Abteilung dieseAbteilung : abt_liste) {
			for (Mitarbeiter dieserMitarbeiter : dieseAbteilung.getMitarb_liste()) {
				if (dieserMitarbeiter.getId() == mitarbeiter_id)
					return dieserMitarbeiter;
			}
		}

		return null;
	}
}
