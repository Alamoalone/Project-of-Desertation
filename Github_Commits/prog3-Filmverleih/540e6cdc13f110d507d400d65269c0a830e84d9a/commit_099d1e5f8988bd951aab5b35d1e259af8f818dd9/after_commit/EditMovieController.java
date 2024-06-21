package com.filmverleih.filmverleih;

import com.filmverleih.filmverleih.entity.Movies;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.math.BigDecimal;

import java.util.ArrayList;

import static java.lang.String.valueOf;

public class EditMovieController {
    private NWayControllerConnector<NavbarController,
                                    LibraryController,
                                    MovieController,
                                    RentalController,
                                    SettingsController,
                                    FilterController,
                                    CartController,
                                    EditMovieController,
                                    Integer,
                                    Integer> connector;
    /**
     * sets NWayControllerConnector as active connector for this controller, called from MainApplication
     * @param connector the controller passed by MainApplication
     */
    public void setConnector(NWayControllerConnector<NavbarController,
                                                    LibraryController,
                                                    MovieController,
                                                    RentalController,
                                                    SettingsController,
                                                    FilterController,
                                                    CartController,
                                                    EditMovieController,
                                                    Integer,
                                                    Integer> connector) {
        this.connector = connector;
    }

    @FXML
    private StackPane stp_movieEditBackStackPane;
    @FXML
    private AnchorPane acp_EditMovieBackground;
    @FXML
    private GridPane grp_movieEditParamSelect;
    @FXML
    private TextField txf_movieEditID;
    @FXML
    private Label lbl_movieEditNameTitle;
    @FXML
    private TextField txf_movieEditName;
    @FXML
    private Label lbl_movieEditYearTitle;
    @FXML
    private TextField txf_movieEditYear;

    @FXML
    private Label lbl_movieEditLengthTitle;
    @FXML
    private TextField txf_movieEditLength;
    @FXML
    private Label lbl_movieEditFskTitle;
    @FXML
    private TextField txf_movieEditFSK;
    @FXML
    private Label lbl_movieEditRatingTitle;
    @FXML
    private TextField txf_movieEditRating;
    @FXML
    private Label lbl_movieEditGenre1Title;
    @FXML
    private TextField txf_movieEditGenre1;
    @FXML
    private Label lbl_movieEditGenre2Title;
    @FXML
    private TextField txf_movieEditGenre2;
    @FXML
    private TextField txf_movieEditGenre3;
    @FXML
    private Label lbl_movieEditDirector1Title;
    @FXML
    private TextField txf_movieEditDirector1;
    @FXML
    private Label lbl_movieEditDirector2Title;
    @FXML
    private TextField txf_movieEditDirector2;
    @FXML
    private TextField txf_movieEditDirector3;
    @FXML
    private TextField txf_movieEditCount;
    @FXML
    private Label lbl_movieEditCountTitle;
    @FXML
    private TextField txf_movieEditStudio;
    @FXML
    private Label lbl_movieEditActorsTitle;
    @FXML
    private TextField txf_movieEditActors;
    @FXML
    private Label lbl_movieEditLinkToCoverTitle;
    @FXML
    private TextField txf_movieEditLinkToCover;
    @FXML
    private TextArea txa_movieEditComment;
    @FXML
    private ImageView igv_movieEditNameUndo;
    @FXML
    private ImageView igv_movieEditYearUndo;
    @FXML
    private ImageView igv_movieEditLengthUndo;
    @FXML
    private ImageView igv_movieEditFskUndo;
    @FXML
    private ImageView igv_movieEditRatingUndo;
    @FXML
    private ImageView igv_movieEditGenre1Undo;
    @FXML
    private ImageView igv_movieEditGenre2Undo;
    @FXML
    private ImageView igv_movieEditGenre3Undo;
    @FXML
    private ImageView igv_movieEditDirector1Undo;
    @FXML
    private ImageView igv_movieEditDirector2Undo;
    @FXML
    private ImageView igv_movieEditDirector3Undo;
    @FXML
    private ImageView igv_movieEditCountUndo;
    @FXML
    private ImageView igv_movieEditStudioUndo;
    @FXML
    private ImageView igv_movieEditActorsUndo;
    @FXML
    private ImageView igv_movieEditLinkToCoverUndo;
    @FXML
    private ImageView igv_movieEditCommentUndo;
    @FXML
    private CheckBox cbx_movieEditSelDVD;
    @FXML
    private CheckBox cbx_movieEditSelBluRay;
    @FXML
    private Button btn_confirmMovieEdit;
    @FXML
    private Label lbl_movieEditSaveFeedback;
    @FXML
    private Button btn_cancelMovieEdit;
    @FXML
    private Button btn_deleteMovieEdit;
    @FXML
    private Button btn_movieEditDeleteConfirm;
    @FXML
    private Label lbl_movieEditDeleteFeedback;
    @FXML
    private AnchorPane acp_movieEditDeleteConfirmation;

    private Movies movie;
    private ArrayList<ArrayList<Object>> txfStringUndoList = new ArrayList<>();
    private final String MOVIE_DELETE_SUCCESSFUL = "Der Film wurde erfolgreich entfernt.";
    private final String MOVIE_DELETE_FAILED = "Es ist etwas schiefgelaufen! Versuchen Sie es bitte erneut.";
    private final String MOVIE_SAVE_SUCCESSFUL = "Die Daten wurden erfolgreich gespeichert.";
    private final String MOVIE_SAVE_FAILED = "Manche Textfelder stimmen nicht mit den Anforderungen überein. " +
            "Die Titel dieser wurden rot markiert.";
    private final String MOVIE_SAVE_WENT_WRONG = "Etwas ist schiefgelaufen, bitte versuchen Sie es erneut.";

    private final double MAX_RATING = 10;
    private final double MIN_RATING = 0;

    private int currentMovieId;
    private String currentMovieName;
    private int currentMovieYear;
    private int currentMovieDuration;
    private int currentMovieFSK;
    private BigDecimal currentMovieRating;
    private String currentMovieGenres;
    private String currentMovieDirectors;
    private int currentMovieCount;
    private String currentMovieStudio;
    private String currentMovieActors;
    private String currentLinkToCover;
    private String currentMovieComment;
    private String currentMovieType;
    private String[] genreArray = new String[10];
    private String[] directorsArray = new String[10];

    private String changedName;
    private int changedYear;
    private int changedLength;
    private int changedFsk;
    private BigDecimal changedRating;
    private String changedGenres;
    private String changedDirectors;
    private int changedCount;
    private String changedStudio;
    private String changedActors;
    private String changedLinkToCover;
    private String changedComment;
    private String changedType;


    public void initialize(Movies movie) {
        this.movie = movie;
        txfStringUndoList.clear();
        movieDataGetter();
        splitGenreDirectorsToArray();
        txfListFiller();
        txfListenerInitializer();
        txaListenerInitializer();
        insertMovieData();
        addOnlyNumbersConstraint();
        undoButtonAddEventHandler();
        checkBoxAddEventHandler();
    }

    private void splitGenreDirectorsToArray() {
        if(currentMovieGenres != null) {
            this.genreArray = currentMovieGenres.split(", ");
        }
        if(currentMovieDirectors != null) {
            this.directorsArray = currentMovieDirectors.split(", ");
        }
    }

    private void movieDataGetter() {
        this.currentMovieId = movie.getMovieid();
        this.currentMovieName = movie.getName();
        this.currentMovieYear = movie.getYear();
        this.currentMovieDuration = movie.getLength();
        this.currentMovieFSK = movie.getFsk();
        this.currentMovieRating = movie.getRating();
        this.currentMovieGenres = movie.getGenre();
        this.currentMovieDirectors = movie.getDirectors();
        this.currentMovieCount = movie.getCount();
        this.currentMovieStudio = movie.getStudio();
        this.currentMovieActors = movie.getActors();
        this.currentLinkToCover = movie.getCover();
        this.currentMovieComment = movie.getComment();
        this.currentMovieType = movie.getType();
    }

    private void insertMovieData() {
        txf_movieEditID.setText(valueOf(currentMovieId));
        txf_movieEditName.setText(currentMovieName);
        txf_movieEditYear.setText(valueOf(currentMovieYear));
        txf_movieEditLength.setText(valueOf(currentMovieDuration));
        txf_movieEditFSK.setText(valueOf(currentMovieFSK));
        txf_movieEditRating.setText(valueOf(currentMovieRating));

        if(currentMovieGenres != null) {
            txf_movieEditGenre1.setText(genreArray[0]);
        } //txf_movieEditGenre1.setText();
        if(genreArray.length > 1) {
            txf_movieEditGenre2.setText(genreArray[1]);
        } //else {txf_movieEditGenre2.setText(null);}
        if (genreArray.length > 2) {
            txf_movieEditGenre3.setText(genreArray[2]);
        } //else {txf_movieEditGenre3.setText(null);}

        if(currentMovieDirectors != null) {
            txf_movieEditDirector1.setText(directorsArray[0]);
        }
        if(directorsArray.length > 1) {
            txf_movieEditDirector2.setText(directorsArray[1]);
        } //else {txf_movieEditDirector2.setText(null);}
        if (directorsArray.length > 2) {
            txf_movieEditDirector3.setText(directorsArray[2]);
        } //else {txf_movieEditDirector3.setText(null);}

        txf_movieEditCount.setText(valueOf(currentMovieCount));
        txf_movieEditStudio.setText(currentMovieStudio);
        txf_movieEditActors.setText(currentMovieActors);
        txf_movieEditLinkToCover.setText(currentLinkToCover);               //TODO akzeptierende Regx für URL
        txa_movieEditComment.setText(currentMovieComment);
        txa_movieEditComment.setWrapText(true);
        if(currentMovieType.equals("DVD")) {cbx_movieEditSelDVD.setSelected(true);}
        if(currentMovieType.equals("BR")) {cbx_movieEditSelBluRay.setSelected(true);}
    }

    private void addOnlyNumbersConstraint() {
        TextFieldFunctions.addYearChecker(txf_movieEditYear);                 //TODO ^\d{4}$ als akzeptierende Regx
        TextFieldFunctions.addOnlyNumberChecker(txf_movieEditLength);
        TextFieldFunctions.addOnlyNumberChecker(txf_movieEditFSK);
        TextFieldFunctions.addRatingChecker(txf_movieEditRating);               //TODO ^-?\d\.\d$ akzeptierende Regx
        TextFieldFunctions.addOnlyNumberChecker(txf_movieEditCount);
    }

    private void txfListFiller() {
        addToTxfStringList(txf_movieEditName, currentMovieName, igv_movieEditNameUndo);
        addToTxfStringList(txf_movieEditYear, valueOf(currentMovieYear), igv_movieEditYearUndo);
        addToTxfStringList(txf_movieEditLength, valueOf(currentMovieDuration), igv_movieEditLengthUndo);
        addToTxfStringList(txf_movieEditFSK, valueOf(currentMovieFSK), igv_movieEditFskUndo);
        addToTxfStringList(txf_movieEditRating, valueOf(currentMovieRating), igv_movieEditRatingUndo);
        if(genreArray != null) {
            addToTxfStringList(txf_movieEditGenre1, genreArray[0], igv_movieEditGenre1Undo);
        } else {
            addToTxfStringList(txf_movieEditGenre1, null, igv_movieEditGenre1Undo);
        }
        if(genreArray.length > 1) {
            addToTxfStringList(txf_movieEditGenre2, genreArray[1], igv_movieEditGenre2Undo);
        } else {
            addToTxfStringList(txf_movieEditGenre2, null, igv_movieEditGenre2Undo);
        }
        if(genreArray.length > 2) {
            addToTxfStringList(txf_movieEditGenre3, genreArray[2], igv_movieEditGenre3Undo);
        } else {
            addToTxfStringList(txf_movieEditGenre3, null, igv_movieEditGenre3Undo);
        }

        if(directorsArray != null) {
            addToTxfStringList(txf_movieEditDirector1, directorsArray[0], igv_movieEditDirector1Undo);
        } else {
            addToTxfStringList(txf_movieEditDirector1, null, igv_movieEditDirector1Undo);
        }
        if(directorsArray.length > 1) {
            addToTxfStringList(txf_movieEditDirector2, directorsArray[1], igv_movieEditDirector2Undo);
        } else {
            addToTxfStringList(txf_movieEditDirector2, null, igv_movieEditDirector2Undo);
        }
        if(directorsArray.length > 2) {
            addToTxfStringList(txf_movieEditDirector3, directorsArray[2], igv_movieEditDirector3Undo);
        } else {
            addToTxfStringList(txf_movieEditDirector3, null, igv_movieEditDirector3Undo);
        }
        addToTxfStringList(txf_movieEditCount, valueOf(currentMovieCount), igv_movieEditCountUndo);
        addToTxfStringList(txf_movieEditStudio, currentMovieStudio, igv_movieEditStudioUndo);
        addToTxfStringList(txf_movieEditActors, currentMovieActors, igv_movieEditActorsUndo);
        addToTxfStringList(txf_movieEditLinkToCover, currentLinkToCover, igv_movieEditLinkToCoverUndo);
    }

    private void addToTxfStringList(TextField textField, String data, ImageView undoButton) {
        ArrayList<Object> tempList = new ArrayList<>();
        tempList.add(textField);
        tempList.add(data);
        tempList.add(undoButton);
        txfStringUndoList.add(tempList);
    }

    private void txfListenerInitializer() {
        for(ArrayList<Object> tempRow : txfStringUndoList) {
            TextField tempTextField = (TextField) tempRow.getFirst();
                tempTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                    String tempCurrentMovieData = (String) tempRow.get(1);
                    if(newValue == null && tempCurrentMovieData == null) {
                        tempTextField.setStyle("-fx-text-fill: #FFF");
                    }
                    if(newValue != null) {
                        if (newValue.equals(tempCurrentMovieData)) {
                            tempTextField.setStyle("-fx-text-fill: #FFF");
                        } else {
                            tempTextField.setStyle("-fx-text-fill: #FF4040; -fx-prompt-text-fill: #FF4040");
                        }
                    }
                });
        }
    }

    private void txaListenerInitializer() {
        txa_movieEditComment.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(currentMovieComment)) {
                txa_movieEditComment.setStyle("-fx-text-fill: #FFF");
            } else {
                txa_movieEditComment.setStyle("-fx-text-fill: #FF4040; -fx-prompt-text-fill: #FF4040");
            }
        });
    }

    private void undoButtonAddEventHandler() {
        for(ArrayList<Object> tempRow : txfStringUndoList) {
            TextField tempTextField = (TextField) tempRow.getFirst();
            String tempCurrentMovieData = (String) tempRow.get(1);
            ImageView tempUndoIcon = (ImageView) tempRow.get(2);
            tempUndoIcon.setOnMouseClicked(event -> tempTextField.setText(tempCurrentMovieData));
        }
        igv_movieEditCommentUndo.setOnMouseClicked(event -> txa_movieEditComment.setText(currentMovieComment));
    }

    private void checkBoxAddEventHandler() {
        cbx_movieEditSelDVD.setOnAction(event -> {
            cbx_movieEditSelBluRay.setSelected(!cbx_movieEditSelDVD.isSelected());
            checkIfCheckBoxesChanged();
        });
        cbx_movieEditSelBluRay.setOnAction(event -> {
            cbx_movieEditSelDVD.setSelected(!cbx_movieEditSelBluRay.isSelected());
            checkIfCheckBoxesChanged();
        });
    }

    private void checkIfCheckBoxesChanged() {
        if(currentMovieType.equals("DVD") && !cbx_movieEditSelDVD.isSelected()) {
            cbx_movieEditSelDVD.setStyle("-fx-text-fill: #FF4040");
            cbx_movieEditSelBluRay.setStyle("-fx-text-fill: #FF4040");
        } else if(currentMovieType.equals("BR") && !cbx_movieEditSelBluRay.isSelected()) {
            cbx_movieEditSelBluRay.setStyle("-fx-fill: #FF4040");
            cbx_movieEditSelDVD.setStyle("-fx-text-fill: #FF4040");
        } else {
            cbx_movieEditSelBluRay.setStyle("-fx-fill: #FFF");
            cbx_movieEditSelDVD.setStyle("-fx-text-fill: #FFF");
        }
    }

    public void confirmMovieEdit() {
        if(validEntryChecker()) {
            boolean dbUpdateSuccessful = Utility.UpdateMovieInDB(currentMovieId,
                    changedName,
                    changedYear,
                    changedLength,
                    changedFsk,
                    changedRating,
                    changedGenres,
                    changedDirectors,
                    changedCount,
                    changedStudio,
                    changedActors,
                    changedLinkToCover,
                    changedComment,
                    changedType);
            if (dbUpdateSuccessful) {
                lbl_movieEditSaveFeedback.setText(MOVIE_SAVE_SUCCESSFUL);
                lbl_movieEditSaveFeedback.setStyle("-fx-text-fill: #518E21");
                lbl_movieEditSaveFeedback.setVisible(true);
            } else {
                lbl_movieEditSaveFeedback.setText(MOVIE_SAVE_WENT_WRONG);
                lbl_movieEditSaveFeedback.setStyle("-fx-text-fill: #FF4040");
                lbl_movieEditSaveFeedback.setVisible(true);
            }
        } else {
            lbl_movieEditSaveFeedback.setText(MOVIE_SAVE_FAILED);
            lbl_movieEditSaveFeedback.setStyle("-fx-text-fill: #FF4040");
            lbl_movieEditSaveFeedback.setVisible(true);
        }
    }

    private boolean validEntryChecker() {
        boolean entriesAreValid = true;
        saveInfosAsNeededDataTypes();
        try {
            if (changedName.isEmpty()) {
                lbl_movieEditNameTitle.setStyle("-fx-text-fill: #FF4040");
                entriesAreValid = false;
            } else {
                lbl_movieEditNameTitle.setStyle("-fx-text-fill: #949494");
            }

            if (changedYear < 1920 || changedYear > 2024) {
                lbl_movieEditYearTitle.setStyle("-fx-text-fill: #FF4040");
                entriesAreValid = false;
            } else {
                lbl_movieEditYearTitle.setStyle("-fx-text-fill: #949494");
            }

            if (changedFsk != 0 && changedFsk != 6 && changedFsk != 12 && changedFsk != 16 && changedFsk != 18) {
                lbl_movieEditFskTitle.setStyle("-fx-text-fill: #FF4040");
                entriesAreValid = false;
            } else {
                lbl_movieEditFskTitle.setStyle("-fx-text-fill: #949494");
            }

            if (!txf_movieEditRating.getText().matches("^\\d\\.\\d$")) {//changedRating.doubleValue() < MIN_RATING || changedRating.doubleValue() >= MAX_RATING
                lbl_movieEditRatingTitle.setStyle("-fx-text-fill: #FF4040");
                entriesAreValid = false;
            } else {
                lbl_movieEditRatingTitle.setStyle("-fx-text-fill: #949494");
            }

            if (changedGenres != null) {
                if (txf_movieEditGenre1.getText().isEmpty() && (!txf_movieEditGenre2.getText().isEmpty() || !txf_movieEditGenre3.getText().isEmpty())) {
                    lbl_movieEditGenre1Title.setStyle("-fx-text-fill: #FF4040");
                    entriesAreValid = false;
                } else {
                    lbl_movieEditGenre1Title.setStyle("-fx-text-fill: #949494");
                }

                if (txf_movieEditGenre2.getText().isEmpty() && !txf_movieEditGenre3.getText().isEmpty()) {
                    lbl_movieEditGenre2Title.setStyle("-fx-text-fill: #FF4040");
                    entriesAreValid = false;
                } else {
                    lbl_movieEditGenre2Title.setStyle("-fx-text-fill: #949494");
                }
            }

            if (!changedActors.matches("^(\\w+\\.?\\w*( \\w+\\.?\\w*)*(, \\w+\\.?\\w*( \\w+\\.?\\w*)*)*)+$")) {//^(\w+( \w+)*(, \w+( \w+)*)*)+$
                lbl_movieEditActorsTitle.setStyle("-fx-text-fill: #FF4040");
                entriesAreValid = false;
            } else {
                lbl_movieEditActorsTitle.setStyle("-fx-text-fill: #949494");
            }

            if (changedDirectors != null) {
                if (txf_movieEditDirector1.getText().isEmpty() && (!txf_movieEditDirector2.getText().isEmpty() || !txf_movieEditDirector3.getText().isEmpty())) {
                    lbl_movieEditDirector1Title.setStyle("-fx-text-fill: #FF4040");
                    entriesAreValid = false;
                } else {
                    lbl_movieEditDirector1Title.setStyle("-fx-text-fill: #949494");
                }

                if (txf_movieEditDirector2.getText().isEmpty() && !txf_movieEditDirector3.getText().isEmpty()) {
                    lbl_movieEditDirector2Title.setStyle("-fx-text-fill: #FF4040");
                    entriesAreValid = false;
                } else {
                    lbl_movieEditDirector2Title.setStyle("-fx-text-fill: #949494");
                }
            }

            if (!changedLinkToCover.isEmpty() && !changedLinkToCover.matches("^(http://|https://)[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*((\\.jpg)|(\\.png)))$")) {
                lbl_movieEditLinkToCoverTitle.setStyle("-fx-text-fill: #FF4040");
                entriesAreValid = false;
            } else {
                lbl_movieEditLinkToCoverTitle.setStyle("-fx-text-fill: #949494");
            }
        } catch (NullPointerException e) {
            entriesAreValid = false;
        }

        return entriesAreValid;
    }

    private void saveInfosAsNeededDataTypes() {
        this.changedName = txf_movieEditName.getText();
        this.changedYear = Integer.parseInt(txf_movieEditYear.getText());
        this.changedLength = Integer.parseInt(txf_movieEditLength.getText());
        this.changedFsk = Integer.parseInt(txf_movieEditFSK.getText());
        this.changedRating = BigDecimal.valueOf(Double.parseDouble(txf_movieEditRating.getText()));
        this.changedGenres = txf_movieEditGenre1.getText();
        if(!txf_movieEditGenre1.getText().isEmpty() && !txf_movieEditGenre2.getText().isEmpty()) {
            this.changedGenres = this.changedGenres + ", " + txf_movieEditGenre2.getText();
            if (!txf_movieEditGenre3.getText().isEmpty()) {
                this.changedGenres = this.changedGenres + ", " + txf_movieEditGenre3.getText();
            }
        }
        this.changedDirectors = txf_movieEditDirector1.getText();
        if(!txf_movieEditDirector1.getText().isEmpty() && !txf_movieEditDirector2.getText().isEmpty()) {
            this.changedDirectors = this.changedDirectors + ", " + txf_movieEditDirector2.getText();
            if (!txf_movieEditDirector3.getText().isEmpty()) {
                this.changedDirectors = this.changedDirectors + ", " + txf_movieEditDirector3.getText();
            }
        }
        this.changedCount = Integer.parseInt(txf_movieEditCount.getText());
        this.changedStudio = txf_movieEditStudio.getText();
        this.changedActors = txf_movieEditActors.getText();
        this.changedLinkToCover = txf_movieEditLinkToCover.getText();
        this.changedComment = txa_movieEditComment.getText();
        if(cbx_movieEditSelDVD.isSelected()) {
            this.changedType = "DVD";
        } else {this.changedType = "BR";}
    }

    public void cancelMovieEdit() {
        MovieController movieController = connector.getMovieController();
        MainApplication.borderPane.setCenter(movieController.getOuterPane());
        movieController.fillPage(Utility.getMovieById(movie.getMovieid()));
    }

    public void deleteMovie() {/*
        Boolean movieIsDeleted = Utility.DeleteMovieInDB(movie.getMovieid());
        if (movieIsDeleted) {
            lbl_movieEditDeleteFeedback.setText(MOVIE_DELETE_SUCESSFULL);
            lbl_movieEditDeleteFeedback.setVisible(true);
            btn_movieEditDeleteConfirm.setDisable(true);
            this.movie = null;
        } else {
            lbl_movieEditDeleteFeedback.setText(MOVIE_DELETE_FAILED);
            lbl_movieEditDeleteFeedback.setVisible(true);
            btn_movieEditDeleteConfirm.setDisable(true);
        } TODO muss mal gucken eine updateMovieList methode im LibraryController wäre echt geil, um fehler zu verhindern dann kann man nach einem erfolgreichen delete in die einzel movie ansicht geworfen werden wo man den edit button dann deactivate
    */}

    public void openDeleteConfirmation() {
        disableEditNodes();
        enableDeleteConfirmation();
    }

    public void cancelMoveDeletion() {
        enableEditNodes();
        disableDeleteConfirmation();
    }

    private void enableEditNodes() {
        for(ArrayList<Object> tempRow : txfStringUndoList) {
            ImageView tempUndoButton = (ImageView) tempRow.get(2);
            tempUndoButton.setOpacity(100);
        }
        acp_EditMovieBackground.setDisable(false);
        connector.getNavbarController().enableNavBar();
    }

    private void disableEditNodes() {
        for(ArrayList<Object> tempRow : txfStringUndoList) {
            ImageView tempUndoButton = (ImageView) tempRow.get(2);
            tempUndoButton.setOpacity(0.3);
        }
        acp_EditMovieBackground.setDisable(true);
        connector.getNavbarController().disableNavBar();
    }

    private void enableDeleteConfirmation() {
        acp_movieEditDeleteConfirmation.setDisable(false);
        acp_movieEditDeleteConfirmation.setVisible(true);
    }

    private void disableDeleteConfirmation() {
        acp_movieEditDeleteConfirmation.setDisable(true);
        acp_movieEditDeleteConfirmation.setVisible(false);
    }

    public StackPane getOuterPane() {
        return stp_movieEditBackStackPane;
    }
}
