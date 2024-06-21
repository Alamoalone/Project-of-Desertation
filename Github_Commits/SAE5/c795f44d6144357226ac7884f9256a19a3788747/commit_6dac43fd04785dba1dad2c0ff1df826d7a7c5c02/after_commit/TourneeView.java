package org.SAE.Tournee;

import org.SAE.Depot.Depot;
import org.SAE.Depot.JourSemaine;
import org.SAE.Main.BaseView;
import org.SAE.Main.UButton;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class TourneeView extends BaseView<Tournee> {

	public TourneeView() {
		super("Tournee");
		add(topPanel, "North");
		add(mainPanel, "Center");
		add(bottomPanel, "South");
		displayView(false);
	}

	@Override
	protected JPanel createListPanel(Tournee t) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 2));
		JLabel label = new JLabel(t.toString());
		UButton editButton = new UButton("Détailler");
		editButton.addActionListener(e -> {
			displayView(true);
			clear();
			mainPanel.add(createEditPanel(t));
			refresh();
		});
		UButton deleteButton = new UButton("Supprimer");
		deleteButton.addActionListener(e -> {
			t.delete();
			t.loadFromDatabase();
			displayView(false);
		});
		UButton visualiserButton = new UButton("Visualiser");
		visualiserButton.addActionListener(e -> {
			TourneeVisualisation visu = new TourneeVisualisation();
			for (Depot depot : t.depots) {
				double[] geo = TourneeVisualisation.getGeo(depot.adresse.toString());
				visu.addMarker(geo[0], geo[1]);
			}
		});
		panel.add(label);
		panel.add(editButton);
		panel.add(deleteButton);
		panel.add(visualiserButton);
		return panel;
	}
	@Override
	protected ArrayList<Tournee> getList() {
		return Tournee.tournees;
	}

	@Override
	protected JPanel createFormPanel() {
		return getPanel(null);
	}


	@Override
	protected JPanel createEditPanel(Tournee tournee) {
		return getPanel(tournee);
	}

	private JPanel getPanel(Tournee tournee) {
		JPanel panel = new JPanel();
		JTextField nomField = new JTextField();
		panel.setLayout(new GridLayout(0, 2));
		JComboBox<JourSemaine> jourLivraisonComboBox = new JComboBox<>();
		JComboBox<JourSemaine> jourPreparationComboBox = new JComboBox<>();
		for (JourSemaine jour : JourSemaine.values()) {
			jourLivraisonComboBox.addItem(jour);
			jourPreparationComboBox.addItem(jour);
		}
		JCheckBox estLivreMatinCheckBox = new JCheckBox();
		JComboBox<Color> colorComboBox = new JComboBox<>();
		for (Color color : Color.values()) colorComboBox.addItem(color);
		panel.add(new JLabel("Nom"));
		panel.add(nomField);
		panel.add(new JLabel("Jour de livraison"));
		panel.add(jourLivraisonComboBox);
		panel.add(new JLabel("Jour de préparation"));
		panel.add(jourPreparationComboBox);
		panel.add(new JLabel("Couleur"));
		panel.add(colorComboBox);
		panel.add(new JLabel("Est livré le matin"));
		panel.add(estLivreMatinCheckBox);
		if (tournee != null){
		 jourLivraisonComboBox.setSelectedItem(tournee.jourLivraison);
		 jourPreparationComboBox.setSelectedItem(tournee.jourPreparation);
		 estLivreMatinCheckBox.setSelected(tournee.estLivreMatin);
		 colorComboBox.setSelectedItem(Color.valueOf(tournee.color));
		 nomField.setText(tournee.nom);
		}
		else {
			tournee = new Tournee(JourSemaine.Lundi, JourSemaine.Lundi, "", "Rouge", false);
			Tournee.create(tournee);
		}
		JLabel depotsLabel = new JLabel("Depots");
		panel.add(depotsLabel);
		JPanel depotsPanel = new JPanel();
		depotsPanel.setLayout(new GridLayout(0, 2));
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new GridLayout(0, 1));
		JScrollPane scrollPane = new JScrollPane(listPanel);
		scrollPane.setPreferredSize(new Dimension(300, 50));
		panel.add(scrollPane);
		fillDepot(tournee, listPanel);
		Tournee finalTournee = tournee;
		jourLivraisonComboBox.addActionListener(e -> {
			listPanel.removeAll();
			finalTournee.jourLivraison = (JourSemaine) jourLivraisonComboBox.getSelectedItem();
			fillDepot(finalTournee, listPanel);
			listPanel.revalidate();
			listPanel.repaint();
		});
		UButton submitButton = new UButton("Valider");
		submitButton.addActionListener(e -> {
			finalTournee.nom = nomField.getText();
			finalTournee.jourLivraison = (JourSemaine) jourLivraisonComboBox.getSelectedItem();
			finalTournee.jourPreparation = (JourSemaine) jourPreparationComboBox.getSelectedItem();
			finalTournee.estLivreMatin = estLivreMatinCheckBox.isSelected();
			Tournee.update(finalTournee);
			displayView(false);
		});
		panel.add(submitButton);
		return panel;
	}


	private static void fillDepot(Tournee tournee, JPanel listPanel) {
		for (Depot depot : Depot.depots) {
			boolean contains = false;
			for (JourSemaine jour : depot.jourLivraison) {
				if (jour == tournee.jourLivraison){
					contains = true;
					break;
				}
			}
			if (!contains) {
				tournee.depots.remove(depot);
				continue;
			}
			JCheckBox checkBox = new JCheckBox(depot.toString());
			checkBox.setSelected(tournee.depots.contains(depot));
			listPanel.add(checkBox);
			checkBox.addActionListener(e -> {
				if (checkBox.isSelected()) tournee.depots.add(depot);
				else tournee.depots.remove(depot);
			});
		}
	}
}
