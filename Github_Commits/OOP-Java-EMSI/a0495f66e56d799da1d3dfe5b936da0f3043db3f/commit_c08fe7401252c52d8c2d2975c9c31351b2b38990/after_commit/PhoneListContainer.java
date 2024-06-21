package tp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class PhoneListContainer extends Phone {

	private List<Phone> phoneList;

	// constructeur

	public PhoneListContainer(String nom, int prix, int numserie) {
		super(nom, prix, numserie);
		phoneList = new ArrayList<Phone>();
	}

	public List<Phone> getPhoneList() {
		return phoneList;
	}

	public void setPhoneList(List<Phone> phoneList) {
		this.phoneList = phoneList;
	}

	// a. Fonction pour alimenter la liste

	public void populateList(Phone[] phones) {
		phoneList.addAll(Arrays.asList(phones));
	}

	// b. Fonction pour afficher la liste (ligne par ligne)

	public void displayList() {
		for (Phone phone : phoneList) {
			System.out.println(phone);
		}
	}
	// c. Fonction pour parcourir tous les éléments de la liste à l'aide d'un
	// Iterateur

	public void iterateList() {
		Iterator<Phone> iterator = phoneList.iterator();
		while (iterator.hasNext()) {

		}
	}
	// d. Fonction pour insérer un élément dans la liste

	public void insertElement(Phone phone, int index) {
		phoneList.add(index, phone);
	}

	// e. Codez une fonction Java qui permet de récupérer un élément

	public Phone getElement(int index) {
		return phoneList.get(index);
	}
	// f. Codez une fonction Java qui permet de supprimer un élément.

	public void removeElement(Phone phone) {
		phoneList.remove(phone);
	}
	// g. Codez une fonction Java qui permet de rechercher un élément.

	public boolean searchElement(Phone phone) {
		return phoneList.contains(phone);
	}

	// h. Codez une fonction Java qui permet de trier la liste
	public void sortList() {
		Collections.sort(phoneList);
	}

	// i. Codez une fonction Java qui permet de copier la liste dans un nouveau
	// tableau.

	public List<Phone> copyList() {
		return new ArrayList<>(phoneList);
	}
	// j. Codez une fonction Java qui permet de mélanger les éléments de la liste.

	public void shuffleList() {
		Collections.shuffle(phoneList);
	}

	// k. Codez une fonction Java qui permet d’inverser les éléments de la liste.

	public void reverseList() {
		Collections.reverse(phoneList);
	}

	// l. Codez une fonction Java qui permet d’extraire une partie de la liste.

	public List<Phone> subList(int fromIndex, int toIndex) {
		return phoneList.subList(fromIndex, toIndex);
	}

	// m. Codez une fonction Java qui permet de comparer deux listes.

	public boolean compareLists(List<Phone> otherList) {
		return phoneList.equals(otherList);
	}

	// n. Codez une fonction Java d'échange de deux éléments dans une liste.
	public void swapElements(int iphone7, int iphone8) {
		Collections.swap(phoneList, iphone7, iphone8);
	}

	// o. Codez une fonction Java qui permet de vider la liste.
	public void clearList() {
		phoneList.clear();
	}
	// p. Codez une fonction Java qui permet de tester que la liste est vide ou non.

	public boolean isListEmpty() {
		return phoneList.isEmpty();
	}

}
