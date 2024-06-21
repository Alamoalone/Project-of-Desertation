/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package playlisteditor;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 *
 * @author Andrea
 */
public class Song {
    private String filePath;
    private String fileName;
    private String title;
    private String artist;
    private String album;
    private boolean selected;

    public Song(String filePath, String fileName, String title, String artist, String album, boolean selected) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.selected = selected;
        
        /*Mp3File mp3file = null;
        try {
            mp3file = new Mp3File(filePath);
        } catch (IOException ex) {
            Logger.getLogger(Song.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedTagException ex) {
            Logger.getLogger(Song.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidDataException ex) {
            Logger.getLogger(Song.class.getName()).log(Level.SEVERE, null, ex);
        }
        ID3v2 id3v2Tag = mp3file.getId3v2Tag();
        System.out.println(title + ": " + id3v2Tag.getLength());*/
    }
    
    public Song(String filePath, String fileName, boolean selected) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.selected = selected;
        try {
            Mp3File mp3file = new Mp3File(filePath);
            if (mp3file.hasId3v2Tag()) {
                ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                title = id3v2Tag.getTitle();
                artist = id3v2Tag.getArtist();
                album = id3v2Tag.getAlbum();
                
                File albumCover = new File("cache/albumArts/"+album.replace("?", "")+".jpg");
                if(!albumCover.exists()) {
                    ImageIcon i = new ImageIcon(id3v2Tag.getAlbumImage());
                    ImageIO.write(resizeImageSmooth(i.getImage(), 100, 100), "jpg", new FileOutputStream("cache/albumArts/"+album.replace("?", "")+".jpg"));
                }
                
                File tagCache = new File("cache/songTags/"+fileName);
                BufferedWriter bw = new BufferedWriter(new FileWriter(tagCache));
                bw.write(title);
                bw.newLine();
                bw.write(artist);
                bw.newLine();
                bw.write(album);
                bw.newLine();
                bw.close();
            }       
        } catch (IOException ex) {
            Logger.getLogger(PlaylistEditor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedTagException ex) {
            Logger.getLogger(PlaylistEditor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidDataException ex) {
            Logger.getLogger(PlaylistEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album.replace("?", "");
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    public static BufferedImage resizeImageSmooth(final Image image, int width, int height) {
        ImageIcon resized = new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH));
        final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.drawImage(resized.getImage(), 0, 0, width, height, null);
        graphics2D.dispose();
        return bufferedImage;
    }
    
    
}
