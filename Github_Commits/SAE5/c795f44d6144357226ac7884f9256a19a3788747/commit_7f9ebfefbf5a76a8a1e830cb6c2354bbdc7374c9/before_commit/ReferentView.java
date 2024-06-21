package org.SAE.Referent;

import org.SAE.Main.BaseView;
import org.SAE.Main.UButton;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ReferentView extends BaseView<Referent> {
	public ReferentView() {
		super("Referent");
		setLayout(new BorderLayout());
		add(topPanel, "North");
		add(mainPanel, "Center");
		add(bottomPanel, "South");
		displayView(false);
	}

	@Override
	protected ArrayList<Referent> getList() {
		return (ArrayList<Referent>) Referent.referents;
	}

	@Override
	protected JPanel createFormPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 2));
		// create text fields
		JTextField nomField = new JTextField();
		JTextField telephoneField = new JTextField();
		JTextField emailField = new JTextField();
		// create labels
		JLabel nomLabel = new JLabel("Nom *");
		JLabel telephoneLabel = new JLabel("Telephone *");
		JLabel emailLabel = new JLabel("Email *");
		// add labels and fields to the panel
		panel.add(nomLabel);
		panel.add(nomField);
		panel.add(telephoneLabel);
		panel.add(telephoneField);
		panel.add(emailLabel);
		panel.add(emailField);
		// add a button to create the referent
		UButton createButton = getCreateButton(nomField, telephoneField, emailField);
		panel.add(createButton);
		return panel;
	}

	private UButton getCreateButton(JTextField nomField, JTextField telephoneField, JTextField emailField) {
		UButton createButton = new UButton("Create");
		createButton.addActionListener(e -> {
			Referent r = new Referent(nomField.getText(), telephoneField.getText(), emailField.getText());
			Referent.create(r);
			displayView(false);
		});
		return createButton;
	}

	@Override
	protected JPanel createEditPanel(Referent referent) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 2));
		// create text fields
		JTextField nomField = new JTextField(referent.nom);
		JTextField telephoneField = new JTextField(referent.telephone);
		JTextField emailField = new JTextField(referent.mail);
		// create labels
		JLabel nomLabel = new JLabel("Nom *");
		JLabel telephoneLabel = new JLabel("Telephone *");
		JLabel emailLabel = new JLabel("Email *");
		// add labels and fields to the panel
		panel.add(nomLabel);
		panel.add(nomField);
		panel.add(telephoneLabel);
		panel.add(telephoneField);
		panel.add(emailLabel);
		panel.add(emailField);
		// add a button to create the referent
		UButton createButton = new UButton("Modifier");
		createButton.addActionListener(e -> {

			referent.nom = nomField.getText();
			referent.telephone = telephoneField.getText();
			referent.mail = emailField.getText();
			Referent.update(referent);
			displayView(false);
		});
		panel.add(createButton);
		return panel;
	}
}
