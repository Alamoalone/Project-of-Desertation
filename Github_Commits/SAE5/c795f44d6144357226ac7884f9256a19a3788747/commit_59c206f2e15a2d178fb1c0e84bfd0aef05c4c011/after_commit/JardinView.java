package org.SAE.Jardin;

import org.SAE.Adresse.Adresse;
import org.SAE.Main.BaseView;
import org.SAE.Main.UButton;
import org.SAE.Panier.Panier;
import org.SAE.Panier.PanierView;
import org.SAE.Referent.Referent;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class JardinView extends BaseView<Jardin>{
	public JardinView() {
		super("Jardin");
		setLayout(new BorderLayout());
		// add panels to the frame
		add(topPanel, "North");
		add(mainPanel, "Center");
		add(bottomPanel, "South");
		displayView(false);
	}

	@Override
	protected ArrayList<Jardin> getList() {
		return (ArrayList<Jardin>) Jardin.jardins;
	}

	@Override
	public JPanel createFormPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0, 2));
		// create the form
		JComboBox<Referent> referentField = new JComboBox<>();
		for (Referent referent : Referent.referents) referentField.addItem(referent);
		panel.add(new JLabel("Referent"));
		panel.add(referentField);
		JComboBox<Adresse> adresseSiegeSocialField = new JComboBox<>();
		for (Adresse adresse : Adresse.adresses) adresseSiegeSocialField.addItem(adresse);
		panel.add(new JLabel("Adresse Siege Social"));
		panel.add(adresseSiegeSocialField);
		JComboBox<Adresse> adresseGestionField = new JComboBox<>();
		for (Adresse adresse : Adresse.adresses) adresseGestionField.addItem(adresse);
		panel.add(new JLabel("Adresse Gestion"));
		panel.add(adresseGestionField);
	  JTextField nomCommercialField = new JTextField();
		panel.add(new JLabel("Nom Commercial"));
		panel.add(nomCommercialField);
		JTextField raisonSocialeField = new JTextField();
		panel.add(new JLabel("Raison Sociale"));
		panel.add(raisonSocialeField);
		UButton createButton = new UButton("Créer");
		createButton.addActionListener(e -> {
			Jardin jardin = new Jardin(
				(Referent) referentField.getSelectedItem(),
				(Adresse) adresseSiegeSocialField.getSelectedItem(),
				(Adresse) adresseGestionField.getSelectedItem(),
				nomCommercialField.getText(),
				raisonSocialeField.getText()
			);
			Jardin.create(jardin);
			displayView(false);
		});
		panel.add(createButton);
		return panel;
	}

	@Override
	public JPanel createEditPanel(Jardin jardin) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0, 2));
		// create the form
		JComboBox<Referent> referentField = new JComboBox<>();
		for (Referent referent : Referent.referents) referentField.addItem(referent);
		referentField.setSelectedItem(jardin.referent);
		panel.add(new JLabel("Referent"));
		panel.add(referentField);
		JComboBox<Adresse> adresseSiegeSocialField = new JComboBox<>();
		for (Adresse adresse : Adresse.adresses) adresseSiegeSocialField.addItem(adresse);
		adresseSiegeSocialField.setSelectedItem(jardin.adresseSiegeSocial);
		panel.add(new JLabel("Adresse Siege Social"));
		panel.add(adresseSiegeSocialField);
		JComboBox<Adresse> adresseGestionField = new JComboBox<>();
		for (Adresse adresse : Adresse.adresses) adresseGestionField.addItem(adresse);
		adresseGestionField.setSelectedItem(jardin.adresseGestion);
		panel.add(new JLabel("Adresse Gestion"));
		panel.add(adresseGestionField);
	  JTextField nomCommercialField = new JTextField(jardin.nomCommercial);
		panel.add(new JLabel("Nom Commercial"));
		panel.add(nomCommercialField);
		JTextField raisonSocialeField = new JTextField(jardin.raisonSociale);
		panel.add(new JLabel("Raison Sociale"));
		panel.add(raisonSocialeField);
		// panier
		UButton panierButton = new UButton("Panier");
		panierButton.addActionListener(e -> {
			clear();
			topPanel.remove(createButton);
			topPanel.add(new JLabel("Panier de " + jardin.nomCommercial), "West");
			mainPanel.add(new PanierView(jardin));
			refresh();
		});
		panel.add(panierButton);
		UButton updateButton = new UButton("Update");
		updateButton.addActionListener(e -> {
			jardin.referent = (Referent) referentField.getSelectedItem();
			jardin.adresseSiegeSocial = (Adresse) adresseSiegeSocialField.getSelectedItem();
			jardin.adresseGestion = (Adresse) adresseGestionField.getSelectedItem();
			jardin.nomCommercial = nomCommercialField.getText();
			jardin.raisonSociale = raisonSocialeField.getText();
			Jardin.update(jardin);
			displayView(false);
		});
		panel.add(updateButton);
		return panel;
	}
}
