package org.SAE.Abonnement;

import org.SAE.Client.Client;
import org.SAE.Client.DateLabelFormatter;
import org.SAE.Depot.Depot;
import org.SAE.Main.BaseView;
import org.SAE.Main.UButton;
import org.SAE.Panier.Panier;
import org.jdatepicker.JDatePanel;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Properties;

public class AbonnementView extends BaseView<Abonnement> {

	public AbonnementView() {
		super("Abonnement");
		add(topPanel, "North");
		add(mainPanel, "Center");
		add(bottomPanel, "South");
		displayView(false);
	}

	@Override
	protected ArrayList<Abonnement> getList() {
		return (ArrayList<Abonnement>) Abonnement.abonnements;
	}

	@Override
	protected JPanel createFormPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0, 2));
		JComboBox<Client> clientComboBox = new JComboBox<>();
		for (Client client : Client.clients) clientComboBox.addItem(client);
		JComboBox<Panier> panierComboBox = new JComboBox<>();
		for (Panier panier : Panier.paniers) panierComboBox.addItem(panier);
		Properties p = new Properties();
		p.put("text.today", "Today");
		p.put("text.month", "Month");
		p.put("text.year", "Year");
		JDatePanelImpl datePanelD = new JDatePanelImpl(new UtilDateModel(), p);
		JDatePanelImpl datePanelF = new JDatePanelImpl(new UtilDateModel(), p);
		JDatePickerImpl debutField = new JDatePickerImpl(datePanelD, new DateLabelFormatter());
		JDatePickerImpl finField = new JDatePickerImpl(datePanelF, new DateLabelFormatter());
		JTextField frequenceLivraisonField = new JTextField();
		JCheckBox estActifCheckBox = new JCheckBox();
		panel.add(new JLabel("Client"));
		panel.add(clientComboBox);
		panel.add(new JLabel("Panier"));
		panel.add(panierComboBox);
		panel.add(new JLabel("Date début"));
		panel.add(debutField);
		panel.add(new JLabel("Date fin"));
		panel.add(finField);
		panel.add(new JLabel("Fréquence de livraison"));
		panel.add(frequenceLivraisonField);
		panel.add(new JLabel("Est actif"));
		panel.add(estActifCheckBox);
		JButton createButton = new JButton("Créer");
		createButton.addActionListener(e -> {
			Client client = (Client) clientComboBox.getSelectedItem();
			Panier panier = (Panier) panierComboBox.getSelectedItem();
			// check for value
			if (client == null || panier == null || debutField.getJFormattedTextField().getText().isEmpty() || finField.getJFormattedTextField().getText().isEmpty() || frequenceLivraisonField.getText().isEmpty()) {
				org.SAE.Main.Logger.error("All fields must be filled");
				return;
			}
			// check if date interval is greater than frequenceLivraison
			if (Date.valueOf(debutField.getJFormattedTextField().getText())
							.toLocalDate().plusDays(Integer.parseInt(frequenceLivraisonField.getText()))
							.isAfter(Date.valueOf(finField.getJFormattedTextField().getText()).toLocalDate())) {
				org.SAE.Main.Logger.error("Date interval must be greater than frequenceLivraison");
				return;
			}
			Date debut = Date.valueOf(debutField.getJFormattedTextField().getText());
			Date fin = Date.valueOf(finField.getJFormattedTextField().getText());
			int frequenceLivraison = Integer.parseInt(frequenceLivraisonField.getText());
			boolean estActif = estActifCheckBox.isSelected();
			Abonnement abonnement = new Abonnement(client, panier, debut, fin, frequenceLivraison, estActif);
			Abonnement.create(abonnement);
			displayView(false);
		});
		panel.add(createButton);
		return panel;
	}
	@Override
	protected JPanel createEditPanel(Abonnement abonnement) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0, 2));
		JComboBox<Client> clientComboBox = new JComboBox<>();
		for (Client client : Client.clients) clientComboBox.addItem(client);
		JComboBox<Panier> panierComboBox = new JComboBox<>();
		for (Panier panier : Panier.paniers) panierComboBox.addItem(panier);
		Properties p = new Properties();
		p.put("text.today", "Today");
		p.put("text.month", "Month");
		p.put("text.year", "Year");
		JDatePanelImpl datePaneld = new JDatePanelImpl(new UtilDateModel(), p);
		JDatePickerImpl debutField = new JDatePickerImpl(datePaneld, new DateLabelFormatter());
		debutField.getJFormattedTextField().setText(abonnement.debut.toString());
		JDatePanelImpl datePanelf = new JDatePanelImpl(new UtilDateModel(), p);
		JDatePickerImpl finField = new JDatePickerImpl(datePanelf, new DateLabelFormatter());
		finField.getJFormattedTextField().setText(abonnement.fin.toString());
		JTextField frequenceLivraisonField = new JTextField();
		frequenceLivraisonField.setText(String.valueOf(abonnement.frequenceLivraison));
		JCheckBox estActifCheckBox = new JCheckBox();
		estActifCheckBox.setSelected(abonnement.estActif);
		panel.add(new JLabel("Client"));
		panel.add(clientComboBox);
		panel.add(new JLabel("Panier"));
		panel.add(panierComboBox);
		panel.add(new JLabel("Date début"));
		panel.add(debutField);
		panel.add(new JLabel("Date fin"));
		panel.add(finField);
		panel.add(new JLabel("Fréquence de livraison"));
		panel.add(frequenceLivraisonField);
		panel.add(new JLabel("Est actif"));
		panel.add(estActifCheckBox);
		UButton createButton = new UButton("Update");
		createButton.addActionListener(e -> {
			abonnement.client = (Client) clientComboBox.getSelectedItem();
			abonnement.panier = (Panier) panierComboBox.getSelectedItem();
			// check for value
			if (abonnement.client == null || abonnement.panier == null || debutField.getJFormattedTextField().getText().isEmpty() || finField.getJFormattedTextField().getText().isEmpty() || frequenceLivraisonField.getText().isEmpty()) {
				org.SAE.Main.Logger.error("All fields must be filled");
				return;
			}
			// check if date interval is greater than frequenceLivraison
			if (Date.valueOf(debutField.getJFormattedTextField().getText())
							.toLocalDate().plusDays(Integer.parseInt(frequenceLivraisonField.getText()))
							.isAfter(Date.valueOf(finField.getJFormattedTextField().getText()).toLocalDate())) {
				org.SAE.Main.Logger.error("Date interval must be greater than frequenceLivraison");
				return;
			}
			abonnement.debut = Date.valueOf(debutField.getJFormattedTextField().getText());
			abonnement.fin = Date.valueOf(finField.getJFormattedTextField().getText());
			abonnement.frequenceLivraison = Integer.parseInt(frequenceLivraisonField.getText());
			abonnement.estActif = estActifCheckBox.isSelected();
			Abonnement.update(abonnement);
			displayView(false);
		});
		panel.add(createButton);
		return panel;
	}


}
