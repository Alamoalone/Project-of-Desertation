import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class GUIForm {
    private JPanel rootPanel;
    private JTextField inputFileTextField;
    private JButton inputFileButton;
    private JButton outputFileButton;
    private JTextField outputFileTextField;
    private JLabel titleOne;
    private JCheckBox formatJpg;
    private JCheckBox formatPng;
    private JCheckBox formatWebp;
    private JLabel changeFormat;
    private JLabel ChangeSizq;
    private JTextField heightInPixels;
    private JTextField widthInPixels;
    private JTextField compressionRateField;
    private JCheckBox sortFotos;
    private JButton startButton;
    private JPanel mainPanel;


    private String srcFolder = "";
    private String dstFolder = "";
    ConvertToThisFormats dstFormat;
    private boolean changeFormatBoolean;
    private boolean changeImages;
    private boolean sortPhotos;
    private boolean poxui;
    private int width;
    private int height;
    private int compressionRate;


    public GUIForm() {

        inputFileButton.addActionListener(new Action() {
            @Override
            public Object getValue(String key) {
                return null;
            }

            @Override
            public void putValue(String key, Object value) {

            }

            @Override
            public void setEnabled(boolean b) {

            }

            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {

            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {

            }

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                chooser.showOpenDialog(mainPanel);
                File selectedFile = chooser.getSelectedFile();
                if (selectedFile == null) {
                    inputFileTextField.setText("");
                    srcFolder = "";
                    return;
                }
                srcFolder = selectedFile.getPath();
                inputFileTextField.setText(srcFolder);
            }
        });

        outputFileButton.addActionListener(new Action() {
            @Override
            public Object getValue(String key) {
                return null;
            }

            @Override
            public void putValue(String key, Object value) {

            }

            @Override
            public void setEnabled(boolean b) {

            }

            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {

            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {

            }

            @Override
            public void actionPerformed(ActionEvent e) {

                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                chooser.showOpenDialog(rootPanel);
                File selectedFile = chooser.getSelectedFile();
                if (selectedFile == null) {
                    outputFileTextField.setText("");
                    dstFolder = "";
                    return;
                }
                dstFolder = selectedFile.getPath();
                outputFileTextField.setText(srcFolder);

            }
        });

        formatJpg.addActionListener(new Action() {
            @Override
            public Object getValue(String key) {
                return null;
            }

            @Override
            public void putValue(String key, Object value) {

            }

            @Override
            public void setEnabled(boolean b) {

            }

            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {

            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {

            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (formatJpg.isSelected()) {
                    dstFormat = ConvertToThisFormats.jpg;
                    sortFotos.setEnabled(false);
                    formatPng.setEnabled(false);
                    formatWebp.setEnabled(false);
                    changeFormatBoolean = true;
                } else {
                    sortFotos.setEnabled(true);
                    formatPng.setEnabled(true);
                    formatWebp.setEnabled(true);
                    changeFormatBoolean = false;
                }
            }
        });

        formatPng.addActionListener(new Action() {
            @Override
            public Object getValue(String key) {
                return null;
            }

            @Override
            public void putValue(String key, Object value) {

            }

            @Override
            public void setEnabled(boolean b) {

            }

            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {

            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {

            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (formatPng.isSelected()) {
                    dstFormat = ConvertToThisFormats.png;
                    sortFotos.setEnabled(false);
                    formatJpg.setEnabled(false);
                    formatWebp.setEnabled(false);
                    changeFormatBoolean = true;
                } else {
                    sortFotos.setEnabled(true);
                    formatPng.setEnabled(true);
                    formatWebp.setEnabled(true);
                    changeFormatBoolean = false;
                }
            }
        });

        formatWebp.addActionListener(new Action() {
            @Override
            public Object getValue(String key) {
                return null;
            }

            @Override
            public void putValue(String key, Object value) {

            }

            @Override
            public void setEnabled(boolean b) {

            }

            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {

            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {

            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (formatWebp.isSelected()) {
                    dstFormat = ConvertToThisFormats.webp;
                    sortFotos.setEnabled(false);
                    formatJpg.setEnabled(false);
                    formatPng.setEnabled(false);
                    changeFormatBoolean = true;
                } else {
                    sortFotos.setEnabled(true);
                    formatPng.setEnabled(true);
                    formatJpg.setEnabled(true);
                    changeFormatBoolean = false;
                }
            }
        });


        widthInPixels.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                compressionRateField.setEnabled(false);
                sortFotos.setEnabled(false);
            }

            @Override
            public void focusLost(FocusEvent e) {
                String widthText = widthInPixels.getText();
                if (widthText.isEmpty()) {
                    compressionRateField.setEnabled(true);
                    sortFotos.setEnabled(true);
                } else {
                    try {
                        width = Integer.parseInt(widthText);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(mainPanel, "Неверный формат ввода! Введите число", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Снимаем фокус с текстового поля
                widthInPixels.requestFocusInWindow();
            }
        });

        heightInPixels.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                compressionRateField.setEnabled(false);
                sortFotos.setEnabled(false);
            }

            @Override
            public void focusLost(FocusEvent e) {
                String heightText = heightInPixels.getText();
                if (heightText.isEmpty()) {
                    compressionRateField.setEnabled(true);
                    sortFotos.setEnabled(true);
                } else {
                    try {
                        height = Integer.parseInt(heightText);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(mainPanel, "Неверный формат ввода! Введите число", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Снимаем фокус с текстового поля
                heightInPixels.requestFocusInWindow();
            }
        });

        compressionRateField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                heightInPixels.setEnabled(false);
                widthInPixels.setEnabled(false);
                sortFotos.setEnabled(false);
            }

            @Override
            public void focusLost(FocusEvent e) {
                String compressionText = compressionRateField.getText();
                if (compressionText.isEmpty()) {
                    heightInPixels.setEnabled(true);
                    widthInPixels.setEnabled(true);
                    sortFotos.setEnabled(true);
                } else {
                    try {
                        compressionRate = Integer.parseInt(compressionText);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(mainPanel, "Неверный формат ввода! Введите число", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Снимаем фокус с текстового поля
                compressionRateField.requestFocusInWindow();
            }
        });

        sortFotos.addActionListener(new Action() {
            @Override
            public Object getValue(String key) {
                return null;
            }

            @Override
            public void putValue(String key, Object value) {

            }

            @Override
            public void setEnabled(boolean b) {

            }

            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {

            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {

            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (sortFotos.isSelected()) {
                    formatJpg.setEnabled(false);
                    formatPng.setEnabled(false);
                    formatWebp.setEnabled(false);
                    changeFormatBoolean = false;
                    compressionRateField.setEnabled(false);
                    widthInPixels.setEnabled(false);
                    heightInPixels.setEnabled(false);
                    changeImages = true;
                } else {
                    formatJpg.setEnabled(true);
                    formatPng.setEnabled(true);
                    formatWebp.setEnabled(true);
                    compressionRateField.setEnabled(true);
                    widthInPixels.setEnabled(true);
                    heightInPixels.setEnabled(true);
                    changeImages = true;
                }
            }
        });

        startButton.addActionListener(new Action() {
            @Override
            public Object getValue(String key) {
                return null;
            }

            @Override
            public void putValue(String key, Object value) {

            }

            @Override
            public void setEnabled(boolean b) {

            }

            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {

            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {

            }

            @Override
            public void actionPerformed(ActionEvent e) {
                formatJpg.setEnabled(false);
                formatPng.setEnabled(false);
                formatWebp.setEnabled(false);
                compressionRateField.setEnabled(false);
                widthInPixels.setEnabled(false);
                heightInPixels.setEnabled(false);
                sortFotos.setEnabled(false);
                inputFileTextField.setEnabled(false);
                inputFileButton.setEnabled(false);
                outputFileTextField.setEnabled(false);
                outputFileButton.setEnabled(false);


                File srcDir = new File(srcFolder);
                File[] filesArray = srcDir.listFiles();
                ArrayList<File> files = Main.listFiles(new ArrayList<>(Arrays.asList(filesArray)));

                boolean isEnd = false;
                if (changeImages) {
                    RenameByCreationDate renameByCreationDate = new RenameByCreationDate(files, dstFolder);
                    isEnd = renameByCreationDate.rename();
                } else {

                    ImageChanger imageChanger = new ImageChanger(files, compressionRate, width, height, dstFormat, dstFolder, false);
                    isEnd = imageChanger.handlePictures();
                }

                while (!isEnd) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                JOptionPane.showMessageDialog(mainPanel, "Работа программы завершена", "Успешно!", JOptionPane.PLAIN_MESSAGE);
                formatJpg.setEnabled(true);
                formatPng.setEnabled(true);
                formatWebp.setEnabled(true);
                compressionRateField.setEnabled(true);
                widthInPixels.setEnabled(true);
                heightInPixels.setEnabled(true);
                sortFotos.setEnabled(true);
                inputFileTextField.setEnabled(true);
                inputFileButton.setEnabled(true);
                outputFileTextField.setEnabled(true);
                outputFileButton.setEnabled(true);

            }
        });


    }


        public JPanel getRootPanel() {
        return rootPanel;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

}
