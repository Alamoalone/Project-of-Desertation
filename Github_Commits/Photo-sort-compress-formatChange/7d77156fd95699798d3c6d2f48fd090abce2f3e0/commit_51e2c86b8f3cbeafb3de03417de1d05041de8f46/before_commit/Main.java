import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class Main {
    public static Logger logger = LogManager.getRootLogger();

    public static void main(String[] args) {

            String srcFolder = "";
            String dstFolder = "";
            ConvertToThisFormats dstFormat;

            JFrame frame = new JFrame("My GUI Form");
            frame.setContentPane(new GUIForm().getRootPanel());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(new Dimension(600, 700)); // Установите желаемый размер окна
            frame.setVisible(true);
        }


         public static ArrayList<File> listFiles (ArrayList < File > src) {
            ArrayList <File> files = new ArrayList<>();
            for (File file : src) {
                if (!file.isDirectory()) {
                    files.add(file);
                } else {
                    ArrayList<File> insideFiles = new ArrayList<>(Arrays.asList(file.listFiles()));
                    files.addAll(listFiles(insideFiles));
                }
            }
            return files;
        }


    }
