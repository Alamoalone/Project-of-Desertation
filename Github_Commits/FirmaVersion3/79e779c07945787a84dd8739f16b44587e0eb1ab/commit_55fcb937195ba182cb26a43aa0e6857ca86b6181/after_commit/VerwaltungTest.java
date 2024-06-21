package at.bfi.projekt.firma_v3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class VerwaltungTest {

	@Test
	void testBerechneNettoArbeiter() {

		double TAX_ARB = 0.15;

		Arbeiter arbeiter_0 = new Arbeiter(1, "Klemens", 10, 10, 10, 10);
		Arbeiter arbeiter_1 = new Arbeiter(2, "Karoline", 20, 20, 20, 20);

		double nettoArbeiter_0 = Verwaltung.berechneNettoGehalt(arbeiter_0);
		double nettoArbeiter_1 = Verwaltung.berechneNettoGehalt(arbeiter_1);

		double bruttoTemp = arbeiter_0.berechneBrutto() + arbeiter_1.berechneBrutto();
		double expectedNetto = bruttoTemp - (bruttoTemp * TAX_ARB);

		assertEquals(expectedNetto, (nettoArbeiter_0 + nettoArbeiter_1));

	}

	@Test
	void testBerechneNettoAngestellter() {

		double TAX_ANG = 0.20;

		Angestellter angestellter_1 = new Angestellter(1, "Warinot", 100.0, 10.00, 1.0);

		double actualNetto = Verwaltung.berechneNettoGehalt(angestellter_1);
		double expectedNetto = angestellter_1.berechneBrutto() - (angestellter_1.berechneBrutto() * TAX_ANG);

		assertEquals(expectedNetto, actualNetto);

	}

	@Test
	void testAugabeAbteilungNachID() {

		Mitarbeiter[] mitarbeiter_liste = new Mitarbeiter[5];

		// Erstellung der Arbeiterobjekte
		Arbeiter arbeiter_0 = new Arbeiter(1, "Anneken", 29.1, 109, 94.4, 12.2);
		Arbeiter arbeiter_1 = new Arbeiter(2, "Poldi", 31.7, 97, 101.3, 13.3);
		Arbeiter arbeiter_2 = new Arbeiter(3, "Ottila", 17.2, 83, 44.9, 14.4);
		Angestellter angestellter_3 = new Angestellter(4, "Hardwin", 2800.00, 1309.80, 19.9);
		Angestellter angestellter_4 = new Angestellter(5, "Barthold", 1812.23, 992.20, 20.0);

		// Zuweisung der Objekte zum Array mit der Liste der Mitarbeiter
		mitarbeiter_liste[0] = arbeiter_0;
		mitarbeiter_liste[1] = arbeiter_1;
		mitarbeiter_liste[2] = arbeiter_2;
		mitarbeiter_liste[3] = angestellter_3;
		mitarbeiter_liste[4] = angestellter_4;

		Verwaltung.abt_liste[0] = new Abteilung(1, "PR.",
				new Mitarbeiter[] { mitarbeiter_liste[0], mitarbeiter_liste[2], mitarbeiter_liste[3] });
		Verwaltung.abt_liste[1] = new Abteilung(1, "SALES", new Mitarbeiter[] { mitarbeiter_liste[1] });

		System.out.println("### Wir holen uns die Mitarbeiterliste aus der PR-Abteilung (ID 1): ");
		Mitarbeiter[] gefundeneMitarbeiterPrAbteilung = Verwaltung.getMitarbeiterListeVonAbteilung(1);

		assertEquals(Verwaltung.abt_liste[0].getMitarb_liste(), gefundeneMitarbeiterPrAbteilung);
	}

	@Test
	void testSucheAbteilungsmitarbeiterNachID() {

		Arbeiter arbeiter_0 = new Arbeiter(1, "Schwanhild", 31.7, 97, 101.3, 13.3);
		Arbeiter arbeiter_1 = new Arbeiter(2, "Friedrich", 29.1, 109, 94.4, 12.2);
		Angestellter angestellter_2 = new Angestellter(3, "Leon", 2800.00, 1309.80, 19.9);
		Arbeiter arbeiter_3 = new Arbeiter(4, "Gerlinde", 17.2, 83, 44.9, 14.4);
		Angestellter angestellter_4 = new Angestellter(5, "Lotte", 1812.23, 992.20, 20.0);

		Verwaltung.abt_liste[0] = new Abteilung(1, "PR.",
				new Mitarbeiter[] { arbeiter_0, angestellter_2, angestellter_4 });
		Verwaltung.abt_liste[1] = new Abteilung(1, "SALES", new Mitarbeiter[] { arbeiter_1, arbeiter_3 });

		Mitarbeiter gefundenerMitarbeiter = Verwaltung.searchMitarbeiterAusFirma(4);

		assertEquals(arbeiter_3.getName(), gefundenerMitarbeiter.getName());
	}

}
