package classes;

import java.util.ArrayList;

public class Library {
    public static ArrayList<Person> persons  = new ArrayList<Person>();
    public static Person loggedIn;
    public static  ArrayList<Book> books = new ArrayList<Book>();
    public Library() {
        books.add(new Book("book1","yehia"));
        books.add(new Book("book2","marwan"));
        books.add(new Book("book3","ayman"));
        books.add(new Book("book4","kassab"));
        books.add(new Book("book5","omar"));



        Librarian l0= new Librarian("pass", "Lib", "a", "a", "1", 1, "email1@librarian.com" );
        Librarian l1= new Librarian("pass", "Lib", "b", "b", "2", 2, "email2@librarian.com" );
        Librarian l4= new Librarian("pass", "Lib", "E", "e", "5", 6, "email5@librarian.com" );
        Librarian l3= new Librarian("pass", "Lib", "D", "d", "4", 5, "email4@librarian.com" );
        Librarian l2= new Librarian("pass", "Lib", "c", "c", "3", 4, "email3@librarian.com" );
        persons.add(l0);
        persons.add(l1);
        persons.add(l2);
        persons.add(l3);
        persons.add(l4);


        Reader R0= new Reader("pass", "Read", "a", "a", "1", 1, "email1@reader.com" );
        Reader R1= new Reader("pass", "Read", "b", "b", "2", 2, "email2@reader.com" );
        Reader R2= new Reader("pass", "Read", "E", "e", "5", 6, "email5@reader.com" );
        Reader R3= new Reader("pass", "Read", "D", "d", "4", 5, "email4@reader.com" );
        Reader R4= new Reader("pass", "Read", "c", "c", "3", 4, "email3@reader.com" );
        persons.add(R0);
        persons.add(R1);
        persons.add(R2);
        persons.add(R3);
        persons.add(R4);

    }

    public static Person checkPassword(String email,String password){
        for (int i = 0; i< persons.size(); i++){
            if (persons.get(i).getEmail().equals(email) && persons.get(i).getPassword().equals(password)){
                System.out.println("The person was found");
                System.out.println(persons.get(i).getTYPE());
                return persons.get(i);
            }

        }
        System.out.println("The person is not found");
        return null;
    }

}






