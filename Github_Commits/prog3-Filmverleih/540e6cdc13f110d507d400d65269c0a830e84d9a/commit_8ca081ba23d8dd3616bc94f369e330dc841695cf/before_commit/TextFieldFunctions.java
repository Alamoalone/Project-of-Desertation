package com.filmverleih.filmverleih;

import javafx.scene.control.TextField;

public class TextFieldFunctions {
    public static void addOnlyNumberChecker(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            /*if (!newValue.matches("\\d?")) {
                textField.setText(newValue.replaceAll("\\D", ""));
            }*/
            if (!newValue.matches("^\\d{0,9}$")) {
                textField.setText(oldValue);
            }
        });
    }

    public static void addYearChecker(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            /*if (!newValue.matches("\\d?")) {
                textField.setText(newValue.replaceAll("\\D", ""));
            }*/
            if (!newValue.matches("^\\d{0,4}$")) {
                textField.setText(oldValue);
            }
        });
    }

    public static void addRatingChecker(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("^-?\\d?\\.\\d?$")) {
                textField.setText(oldValue);
            }
        });
    }
}
