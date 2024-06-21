import org.imgscalr.Scalr;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

public class ImageChanger {
    private ArrayList<File> files;
    private int compressionRate;
    private int width;
    private int height;
    private ConvertToThisFormats dstFormat;
    private String dstFolder;
    private String errorFolder;
    boolean changeFormat;
    private int exceptions;
    private BufferedImage image = null;

    public ImageChanger(ArrayList<File> files, int compressionRate, int width, int height,
                        ConvertToThisFormats dstFormat, String dstFolder, boolean changeFormat) {
        this.files = files;
        this.compressionRate = compressionRate;
        this.width = width;
        this.height = height;
        this.dstFormat = dstFormat;
        this.dstFolder = dstFolder;
        errorFolder = dstFolder + "\\error files";
        this.changeFormat = changeFormat;
    }

    public boolean handlePictures() {
        files.forEach(file -> {
            FileFormat fileFormat = new FileFormat(file.getPath());
            String format = fileFormat.getFileFormat();

            String fileName = getFileName(file.getName());
            if (compressionRate > 0) {
                compressImage(file, format, fileName);
            } else cutImageByPixels(changeFormat, file, format, fileName);
        });
        return true;
    }

    private void cutImageByPixels(boolean changeFormat, File file, String format, String fileName) {
        String pathOfNewFile = ""; // for logger
        try {
            image = ImageIO.read(file);
            image = Scalr.resize(image, width, height);

            if (changeFormat) {
                File newFile = new File(dstFolder + "\\" + fileName + "." + dstFormat.toString());
                newFile = escapeDuplicateFiles(newFile, dstFolder, fileName);
                pathOfNewFile = newFile.getPath();
                ImageIO.write(image, dstFormat.toString(), newFile);
            } else {
                File newFile = new File(dstFolder + "\\" + fileName + "." + format);
                newFile = escapeDuplicateFiles(newFile, dstFolder, fileName);
                ImageIO.write(image, format, newFile);
                Main.logger.info("обработали файл " + newFile.getName());
            }
        } catch (Exception e) {
            Main.logger.error("Не удалось сохранить файл " + file.getName() + " Путь = " + pathOfNewFile);
            copyErrorFile(file, format, fileName);
        }

    }


    private void compressImage(File file, String format, String fileName) {
        String pathOfNewFile = ""; // for logger
        try {
            long size = file.length();
            double buffer = (double) compressionRate / 100;
            Long newSize = size - (int) (size * buffer);
            int targetSize = newSize.intValue() / 1024;

            image = ImageIO.read(file);
            Scalr.resize(image, targetSize);

            File newFile = new File(dstFolder + "\\" + file.getName());
            newFile = escapeDuplicateFiles(newFile, dstFolder, fileName);
            pathOfNewFile = newFile.getPath();
            ImageIO.write(image, format, newFile);
            Main.logger.info("обработали файл " + newFile.getName());

        } catch (Exception e) {
            copyErrorFile(file, format, fileName);
            Main.logger.error("Не удалось сохранить файл " + file.getName() + " Путь = " + pathOfNewFile);
        }

    }


    private void copyErrorFile(File file, String format, String fileName) {
        File folder = new File(errorFolder);
        folder.mkdir();
        File errorFile = new File(errorFolder + "\\" + file.getName());
        while (errorFile.exists()) {
            errorFile = new File(errorFolder + "\\" + fileName + exceptions + "." + format);
            exceptions++;
        }

        try {
            Files.copy(file.toPath(), errorFile.toPath());
        } catch (IOException e) {
            Main.logger.error("Не удалось сохранить файл в errorfile!  " + errorFile.getPath()
                    + "Сообщение об ошибке: "+ e.getMessage());
        }
    }

    private File escapeDuplicateFiles(File file, String dstFolder, String fileName) {
        int x = 1;
        while (file.exists()) {
            String buffer = file.getName();
            int dotIndex = buffer.lastIndexOf(".");
            String fileType = buffer.substring(dotIndex);
            file = new File(dstFolder + "\\" + fileName + x + fileType);
            x++;
        }
        return file;
    }

    private String getFileName(String fileNameWithFiletype) {
        int dotIndex = fileNameWithFiletype.lastIndexOf(".");
        return fileNameWithFiletype.substring(0, dotIndex);
    }
}
