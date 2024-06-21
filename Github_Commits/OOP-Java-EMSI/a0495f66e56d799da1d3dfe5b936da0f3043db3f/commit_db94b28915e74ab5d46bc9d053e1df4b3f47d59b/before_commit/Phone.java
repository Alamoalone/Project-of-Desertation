package tp ;

import java.util.*;

public class Phone implements Comparable<Phone>{
	protected static int numserie;
	protected static String nom;
	protected int prix ;
	
	public Phone(String nom ,int prix,int numserie ) {
		super();
		this.numserie=numserie;
		this.nom = nom;
		this.prix = prix;
	
	}

	

	@Override
	public String toString() {
		return "Phone [numserie=" + numserie + ", nom=" + nom + ", prix=" + prix + "]";
	}




	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Phone other = (Phone) obj;
		return Objects.equals(nom, other.nom) && numserie == other.numserie && prix == other.prix;
	}



	@Override
	public int compareTo(Phone o) {
		return Double.compare(numserie, prix);
	}
	
	

		    public static void main(String[] args) {
		        // Création d'objets Phone
		        Phone tele1 = new SmartPhone("iphone1", 2000, 2023);
		        Phone tele2 = new Phone("nokiapile", 200, 1999);
		        Phone tele3 = new Phone("androidphone", 500, 2005);

		        // Création d'un objet PhoneListContainer
		        PhoneListContainer phoneListContainer = new PhoneListContainer(nom, numserie, numserie);

		        // Remplissage de la liste de téléphones dans PhoneListContainer
		        phoneListContainer.populateList(new Phone[]{tele1, tele2, tele3});

		        // Affichage de la liste de téléphones
		        System.out.println("Liste de téléphones :");
		        phoneListContainer.displayList();

		        // Tri de la liste de téléphones
		        System.out.println("\nListe de téléphones triée :");
		        phoneListContainer.sortList();
		        phoneListContainer.displayList();

		        // Insertion d'un nouveau téléphone 
		        Phone tele4 = new Phone("j1", 300, 2022);
		        phoneListContainer.insertElement(tele4, 1);
		        System.out.println("\nListe de téléphones après insertion d'un nouveau téléphone :");
		        phoneListContainer.displayList();

		        // Suppression d'un téléphone
		        phoneListContainer.removeElement(tele2);
		        System.out.println("\nListe de téléphones après suppression d'un téléphone :");
		        phoneListContainer.displayList();

		        Phone nouveauPhone = null;
				// Recherche d'un téléphone
		        System.out.println("\n'NouveauPhone' est-il dans la liste ? " + phoneListContainer.searchElement(nouveauPhone));

		        // Copie de la liste de téléphones
		        List<Phone> listeCopiee = phoneListContainer.copyList();
		        System.out.println("\nListe de téléphones copiée :");
		        listeCopiee.forEach(System.out::println);

		        // Mélange de la liste de téléphones
		        phoneListContainer.shuffleList();
		        System.out.println("\nListe de téléphones mélangée :");
		        phoneListContainer.displayList();

		        // Inversion de la liste de téléphones
		        phoneListContainer.reverseList();
		        System.out.println("\nListe de téléphones inversée :");
		        phoneListContainer.displayList();

		        // Extraction d'une sous-liste
		        List<Phone> sousListe = phoneListContainer.subList(1, 3);
		        System.out.println("\nSous-liste (de l'index 1 à 3) :");
		        sousListe.forEach(System.out::println);

		        // Comparaison de listes
		        List<Phone> autreListe = Arrays.asList(tele1, tele2, tele3);
		        System.out.println("\nLes deux listes sont-elles égales ? " + phoneListContainer.compareLists(autreListe));

		        // Échange d'éléments aux indices 0 et 2
		        phoneListContainer.swapElements(0, 2);
		        System.out.println("\nListe de téléphones après échange d'éléments aux indices 0 et 2 :");
		        phoneListContainer.displayList();

		        // Vidange de la liste de téléphones
		        phoneListContainer.clearList();
		   
		        System.out.println("\nLa liste de téléphones est-elle vide ? " + phoneListContainer.isListEmpty());
		    
		        Phone tele88 = new SmartPhone("iphone1", 2000, 2023);
				Phone tele5 = new Phone("nokiapile", 200,1999);
				
				
				System.out.println(tele88);
				System.out.println(tele5);
		    
		    }
		

		
	}
	
	


