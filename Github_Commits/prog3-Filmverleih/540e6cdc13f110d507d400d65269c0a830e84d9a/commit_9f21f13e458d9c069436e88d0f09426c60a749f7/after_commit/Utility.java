package com.filmverleih.filmverleih;

import com.filmverleih.filmverleih.entity.Movies;
import com.filmverleih.filmverleih.entity.Users;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.event.spi.EventSource;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Utility {
    // Database Utilities

    //Loading/Instantiating
    public static FXMLLoader loadFXML(String str)
    {       FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApplication.class.getResource(str));
            return loader;
    }

    //
    public static List<Movies> getFullMovieList() {
        try (SessionFactory sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
             Session session = sessionFactory.openSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                List<Movies> movies = session.createQuery("FROM Movies ", Movies.class).getResultList();
                transaction.commit();
                return movies;
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                e.printStackTrace(); // replace with logger
            }
        } catch (Exception e) {
            e.printStackTrace(); // replace with logger
        }
        return new ArrayList<Movies>();
    }

    public Boolean newMovieInDB(String name, int year, String genre, int length, BigDecimal rating, int count, String type, String cover, String comment, String directors, String studio, String actors, int fsk, String movieType) {
        try (SessionFactory sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
             Session session = sessionFactory.openSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();

                Movies newMovie = new Movies();
                newMovie.setName(name);
                newMovie.setYear(year);
                newMovie.setGenre(genre);
                newMovie.setLength(length);
                newMovie.setRating(rating);
                newMovie.setCount(count);
                newMovie.setType(type);
                newMovie.setCover(cover);
                newMovie.setComment(comment);
                newMovie.setDirectors(directors);
                newMovie.setStudio(studio);
                newMovie.setActors(actors);
                newMovie.setFsk(fsk);
                newMovie.setType(movieType);

                // Film zur Datenbank hinzufügen
                session.save(newMovie);
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                e.printStackTrace(); // replace with logger
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace(); // replace with logger
            return false;
        }
        return true;
    }

    public static Boolean DeleteMovieInDB(int movDelID) {
        try (SessionFactory sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
             Session session = sessionFactory.openSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();

                session.createQuery("DELETE FROM Movies WHERE movieid = " + movDelID).executeUpdate();

                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                e.printStackTrace(); // replace with logger
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace(); // replace with logger
            return false;
        }
        return true;
    }

    public static Boolean UpdateMovieInDB(int movieid,
                                    String name,
                                    int year,
                                    int length,
                                    int fsk,
                                    BigDecimal rating,
                                    String genres,
                                    String directors,
                                    int count,
                                    String studio,
                                    String actors,
                                    String cover,
                                    String comment,
                                    String type) {
        try (SessionFactory sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
             Session session = sessionFactory.openSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();

                Query query = session.createQuery("UPDATE Movies SET name = :name" +
                        ", year = :year" +
                        ", length = :length" +
                        ", fsk = :fsk" +
                        ", rating = :rating" +
                        ", genre = :genre" +
                        ", directors = :directors" +
                        ", count = :count" +
                        ", studio = :studio" +
                        ", actors = :actors" +
                        ", cover = :cover" +
                        ", comment = :comment" +
                        ", type = :type" +
                        " WHERE movieid = :movieid");

                query.setParameter("name", name);
                query.setParameter("year", year);
                query.setParameter("length", length);
                query.setParameter("fsk", fsk);
                query.setParameter("rating", rating);
                query.setParameter("genre", genres);
                query.setParameter("directors", directors);
                query.setParameter("count", count);
                query.setParameter("studio", studio);
                query.setParameter("actors", actors);
                query.setParameter("cover", cover);
                query.setParameter("comment", comment);
                query.setParameter("type", type);
                query.setParameter("movieid", movieid);

                query.executeUpdate();

                transaction.commit();

            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                e.printStackTrace(); // replace with logger
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace(); // replace with logger
            return false;
        }
        return true;
    }

    public static Movies getMovieById(int movieId) {
        Movies returnMovie = null;
        try (SessionFactory sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
             Session session = sessionFactory.openSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();

                returnMovie = session.createQuery("FROM Movies WHERE movieid =" + movieId, Movies.class).getSingleResult();

                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                e.printStackTrace(); // replace with logger
            }
        } catch (Exception e) {
            e.printStackTrace(); // replace with logger
        }
        return returnMovie;
    }
    Movies getMovieByUrl(String url)
    {
        Movies ret = new Movies();
        for(Movies movie:getFullMovieList())
        {
            if (movie.getCover().equals(url)) ret = movie;
        }
        return ret;
    }
    Movies getMovieByName(String name)
    {
        Movies ret = new Movies();
        for(Movies movie:getFullMovieList())
        {
            if (movie.getName().equals(name)) ret = movie;
        }
        return ret;
    }
    EventHandler mouseHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            Movies mov = new Movies();
            if (mouseEvent.getSource() instanceof ImageView)
            {
                ImageView source = (ImageView) mouseEvent.getSource();
                mov = getMovieByUrl(source.getImage().getUrl());
            }
            else
            {
               Label source  = (Label) mouseEvent.getSource();
               mov = getMovieByName(source.getText());
            }
        }
    };


    /**
     * This method provides a List of all User from the Table Users
     * @return a list with all users
     */
    public static List<Users> getFullUserList() {
        try (SessionFactory sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
             Session session = sessionFactory.openSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                List<Users> users = session.createQuery("FROM Users ", Users.class).getResultList();
                transaction.commit();
                return users;
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  new ArrayList<Users>();
    }

}
