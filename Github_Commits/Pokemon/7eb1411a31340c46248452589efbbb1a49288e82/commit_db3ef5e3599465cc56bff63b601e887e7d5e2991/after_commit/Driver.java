import java.io.*;
import java.util.*;

public class Driver {
    public static void main (String[] args) {


        System.out.println("Welcome!");
        ArrayList <Album> albums = new ArrayList <>();
        try {
            BufferedReader inFile = new BufferedReader(new FileReader("album1.txt"));
            System.out.println(readFile(inFile, albums, false));
            inFile = new BufferedReader(new FileReader("album5.txt"));
            System.out.println(readFile(inFile, albums, false));
        } catch (IOException ignored) {}
        Scanner in = new Scanner(System.in);
        if (getChar(in, "Would you like to add an album?") == 'y') {
            do {
                importAlbum(in, albums, false);
            } while (getChar(in, "Would you like to add more albums?") == 'y');
        }


        Collections.sort(albums);
        int mainMenuChoice;
        while ((mainMenuChoice = displayMenu(in, 0)) != 3) { // loop until exit
            if (mainMenuChoice == 1) {
                subMenuOne(in,albums);
            } else if (mainMenuChoice == 2) {
                subMenuTwo(in,albums);
            } else {
                System.out.println("Goodbye!");
            }
        }

    }
    public static int displayMenu (Scanner in, int menuNum) {
        if (menuNum == 0) {
            System.out.println ("----------  MAIN MENU  -----------");
            System.out.println ("1) Accessing your list of albums");
            System.out.println ("2) Accessing within a particular album");
            System.out.println ("3) Exit");
            System.out.println ("----------------------------------");
            return getInt(in, "\nPlease enter your choice", 1, 3);
        } else if (menuNum == 1) {
            System.out.println ("\n---------  SUB-MENU #1  ----------");
            System.out.println ("1) Display a list of all albums");
            System.out.println ("2) Display info on a particular album");
            System.out.println ("3) Add an album");
            System.out.println ("4) Remove an album");
            System.out.println ("5) Show statistics");
            System.out.println ("6) Return back to main menu.");
            System.out.println ("----------------------------------");
            return getInt(in, "\nPlease enter your choice", 1, 6);
        } else if (menuNum == 2){
            System.out.println ("\n---------  SUB-MENU #2  ----------");
            System.out.println ("1) Display all cards (in the last sorted order)");
            System.out.println ("2) Display info on a particular card");
            System.out.println ("3) Add a card");
            System.out.println ("4) Remove a card (4 options)");
            System.out.println ("5) Edit attack");
            System.out.println ("6) Sort cards (3 options)");
            System.out.println ("7) Return back to main menu");
            System.out.println ("----------------------------------");
            return getInt(in, "\nPlease enter your choice", 1, 7);
        } else if (menuNum ==3) {
            System.out.println("\n---------  REMOVE ...  ----------");
            System.out.println("1) By Name");
            System.out.println("2) By HP");
            System.out.println("3) First Card in last sorted order");
            System.out.println("4) Last Card in last sorted order");
            return getInt(in, "\nPlease enter your choice", 1, 4);
        } else if (menuNum == 4) {
            System.out.println("\n---------  CHANGE ATTACK ...  ----------");
            System.out.println("1) Name");
            System.out.println("2) Description");
            System.out.println("3) Damage");
            return getInt(in, "\nPlease enter your choice", 1, 3);
        } else {
            System.out.println("\n---------  SORT BY ...  ----------");
            System.out.println("1) Name");
            System.out.println("2) HP");
            System.out.println("3) Date");
            return getInt(in, "\nPlease enter your choice", 1, 3);
        }
    }
    public static void subMenuOne (Scanner in, ArrayList <Album> albums) {
        int choice;
        while ((choice = displayMenu(in, 1)) != 6) {
            if (choice!=3 && albums.isEmpty()) {
                choice = 7;
            }
            switch(choice) {
                case 1: // Display a list of all albums
                    printNameDateAllAlbums(albums);
                    break;
                case 2: // Display info on a particular album
                    printAlbum(in, albums);
                    break;
                case 3: // Add an album
                    importAlbum(in, albums, true);
                    break;
                case 4: // Remove an album
                    removeAlbum(in, albums);
                    break;
                case 5: // Show statistics
                    printStatistics(albums);
                    break;
                case 7:
                    System.out.println("There are no albums imported.");
            }
        }
    }
    public static void subMenuTwo (Scanner in, ArrayList <Album> albums) {
        if (albums.isEmpty()) {
            System.out.println("There are no albums imported.");
            return;
        }
        int choice;
        printAllAlbums(albums);
        Album currentAlbum = albums.get(getAlbumIndex(in,albums));
        while ((choice = displayMenu(in, 2)) != 7) {
            int numOfCardsInAlbum = currentAlbum.getCardsSize();
            if (choice != 3 && numOfCardsInAlbum==0) { choice = 7; } // if empty album and user doesn't want to add cards
            if (numOfCardsInAlbum == 1) {
                System.out.println("Only one card in album, so that card has been automatically chosen");
                if (choice == 1) { choice = 8; } // User wants card name/date
                if (choice == 2) { choice = 9; } // User wants card all info
                if (choice == 4) { choice = 10; } // Remove only card
                if (choice == 5) { choice = 11; } // Edit only card
                if (choice == 6) { choice = 12; } // Sorting won't do anything
            }
            switch(choice) {
                case 1: // Display all cards (in the last sorted order)
                    currentAlbum.printNameDateAllCards(); // prints the name and date
                    break;
                case 2: // Display info on a particular card
                    System.out.println(getCard(in,currentAlbum));
                    break;
                case 3: // Add a card
                    System.out.println(addCard(in,currentAlbum));
                    break;
                case 4: // Remove a card (4 options)
                    removeCard(displayMenu(in, 3),in,currentAlbum);
                    break;
                case 5: // Edit attack
                    editAttack(in,getCard(in,currentAlbum));
                    break;
                case 6: // Sort cards (3 options)
                    sortCards(displayMenu(in,5),currentAlbum);
                    break;
                case 7:
                    System.out.println("There are no cards in album");
                    break;
                case 8: // name date of only card
                    System.out.println(currentAlbum.getCard(0).nameDateToString());
                    break;
                case 9: // all info of only card
                    System.out.println(currentAlbum.getCard(0));
                    break;
                case 10: // remove only card
                    currentAlbum.removeCard(0);
                    break;
                case 11:
                    editAttack(in,currentAlbum.getCard(0));
                    break;
                case 12:
                    System.out.println("Since there is only one card, output will the same no matter which method of sorting" +
                            "is chosen. Here is the card: ");
                    System.out.println(currentAlbum.getCard(0));
            }
        }
    }
    public static Card getCard (Scanner in, Album currentAlbum) {
        currentAlbum.printNameDateAllCards(); // print the name and date
        String message = "Which card would you like to get more information about?";
        return currentAlbum.getCards().get(getInt(in,message,1,currentAlbum.getCardsSize()) - 1);
    }
    public static void printNameDateAllAlbums (ArrayList <Album> albums) {
        for (Album album : albums) {
            System.out.println(album.nameDateToString());
        }
    }
    public static void printAlbum(Scanner in, ArrayList <Album> albums) {
        printNameDateAllAlbums(albums);
        System.out.println(albums.get(getAlbumIndex(in,albums)));
    }
    public static int getAlbumIndex (Scanner in, ArrayList <Album> albums) {
        return Collections.binarySearch(albums, new Album (getAlbumNum(in,albums)));
    }
    public static int getAlbumNum (Scanner in, ArrayList <Album> albums) {
        int min = Integer.MIN_VALUE;
        int max = Integer.MAX_VALUE;
        double n = -1; // stores the input from the user
        int validInt = 0; // int returned to the user
        boolean validAnswer;
        do {
            try {
                System.out.print ("Enter the number of the album looking for: ");
                n = Double.parseDouble (in.nextLine().trim());
                if (n %1.0 == 0) { // if the remainder after dividing by 1 is 0
                    if (n < min) { //less than min
                        n =  1;
                        throw new NumberFormatException();
                    } else if (n > max) { // more than max
                        n = 2;
                        throw new NumberFormatException();
                    } else if (!duplicateAlbum((int) n, albums, true)){
                        n = 3;
                        throw new NumberFormatException();
                    }
                } else {
                    n = -1;
                    throw new NumberFormatException();
                }
                validInt = (int) n;
                validAnswer = true;
            } catch (NumberFormatException e) {
                if (n == -1) { //invalid type
                    System.out.print("ERROR! You entered an Invalid Type. ");
                } else if (n == 1) { // less than min
                    System.out.print("ERROR! Your number cannot be less than " + min + ". ");
                } else if (n==2){ // more than max
                    System.out.print("ERROR! Your number cannot be greater than " + max + ". ");
                } else {
                    System.out.print("ERROR! Your Album number is invalid. ");
                }
                System.out.println("Please choose a valid album number from the ones listed!");
                n = -1;
                validAnswer = false;
            }
        } while (!validAnswer);

        return validInt;
    }
    public static void printAllAlbums (ArrayList <Album> albums) {
        System.out.println("Here are all the albums: ");
        for (Album album : albums) {
            System.out.println(album);
        }
    }
    public static void removeAlbum(Scanner in, ArrayList <Album> albums) {
        printNameDateAllAlbums(albums);
        albums.remove(Collections.binarySearch(albums, new Album (getAlbumNum(in,albums))));
        albums.trimToSize();
    }
    public static void printStatistics (ArrayList <Album> albums) {
        for (Album album : albums) {
            System.out.println(album.averageHP());
            System.out.println(album.cardsOutOfCapacity());
        }
        System.out.println(Album.cardsOutOfCapacityCollection());
        System.out.println(Album.averageHPOfCollection());
    }
    public static String addCard (Scanner in, Album currentAlbum) {
        if (currentAlbum.atMaxCapacity()) {
            return "Sorry, this album is at maximum capacity. You cannot add more cards.";
        } else {
            String name = getString(in, "What is the name of the card?", true);
            int HP = getInt(in, "What is the HP of this card?", 1,Integer.MAX_VALUE);
            String type = getString(in, "What type is this card?",true);
            Date thisCardDate = new Date (getDate(in, "What is the date you got this card? (in MM/DD/YYYY format)"));
            Attack [] attacks = new Attack [getInt(in,"How many attacks does this card have?",
                    1,Integer.MAX_VALUE)];
            for (int j = 0; j < attacks.length; j++) {
                String attackName = getString(in, "What is the name of attack " + j,true);
                String attackDescription = getString(in, "Enter attack description",false);
                String attackDamage = getString(in, "What is the damage of the attack", true);
                attacks[0] = (new Attack(attackName,attackDescription,attackDamage));
            }
            currentAlbum.addCard(new Card (name, HP, type, thisCardDate,attacks));
            return "Card added successfully!";
        }
    }
    public static void removeCard (int choice, Scanner in, Album currentAlbum) {
        switch (choice) {
            case 1: // sort by name, remove
                currentAlbum.sortCardsByName();
                int firstIndexOfName;
                String name;
                do {
                    name = getString(in, "Please give the name of the card you want to remove",
                            true);
                } while ((firstIndexOfName = currentAlbum.getCardOfName(name,true)) < 0);
                int lastIndexOfName = currentAlbum.getCardOfName(name,false);
                currentAlbum.removeCards(firstIndexOfName,lastIndexOfName+1);
                break;
            case 2: // sort by HP, remove
                currentAlbum.sortCardsByHP();
                int firstIndexOfHP;
                int hp;
                do {
                    hp = getInt(in, "Please give the hp of the card you want to remove",
                            1,Integer.MAX_VALUE);
                } while ((firstIndexOfHP = currentAlbum.getCardOfHP(hp,true)) < 0);
                int lastIndexOfHP = currentAlbum.getCardOfHP(hp,false);
                currentAlbum.removeCards(firstIndexOfHP,lastIndexOfHP+1);
                break;
            case 3:
                currentAlbum.removeCard(0);
                break;
            case 4:
                currentAlbum.removeCard(currentAlbum.getCardsSize() - 1);
        }
    }
    public static void editAttack (Scanner in, Card card) {
        Attack[] attacks = card.getAttacks();
        Attack attack;
        if (attacks.length == 1) {
            System.out.println("This card only has one attack. So, it has been automatically chosen.");
            attack = attacks[0];
        } else {
            for (int i = 0; i < attacks.length; i++) {
                System.out.println((i + 1) + ": ");
                System.out.println(attacks[i] + "\n");
            }
            attack = attacks[getInt (in, "Which attack would you like to edit?", 1,attacks.length)];
        }
        int choice = displayMenu(in,4);
        switch (choice) {
            case 1: // name
                attack.edit("name",getString(in,"Enter new name: ",true));
                card.sortAttacks();
                break;
            case 2: // description
                attack.edit("description",getString(in, "Enter new description: ",
                        false));
                break;
            case 3: // description
                attack.edit("damage",getString(in,"Enter new damage: ",true));
        }
    }
    public static void sortCards (int choice, Album currentAlbum) {
        switch (choice) {
            case 1:
                currentAlbum.sortCardsByName();
                break;
            case 2:
                currentAlbum.sortCardsByHP();
                break;
            case 3:
                currentAlbum.sortCardsByDate();
                break;
        }
        currentAlbum.printAllInfoAllCards();
    }
    public static String getString (Scanner in, String message, boolean emptyInputForbidden) {
        String inputString; // stores the input
        boolean validAnswer;
        do {
            try {
                System.out.print(message + ": ");
                inputString = in.nextLine().trim().toLowerCase();

                if (inputString.isEmpty() && emptyInputForbidden) { //input length 0 chars
                    throw new IOException();
                }

                validAnswer = true;
            } catch (IOException e) {
                // input length 0
                System.out.println("ERROR! You did not provide a response.\n");
                inputString = "?";
                validAnswer = false;
            }
        } while (!validAnswer);
        return inputString;
    }
    public static int getInt (Scanner in, String message, int min, int max) {
        if (max<min) {
            min = Integer.MIN_VALUE;
            max = Integer.MAX_VALUE;
        }
        double n = -1; // stores the input from the user
        int validInt = 0; // int returned to the user
        boolean validAnswer;
        do {
            try {
                System.out.print (message + ": ");
                n = Double.parseDouble (in.nextLine().trim());
                if (n %1.0 == 0) { // if the remainder after dividing by 1 is 0
                    if (n < min) { //less than min
                        n =  1;
                        throw new NumberFormatException();
                    } else if (n > max) { // more than max
                        n = 2;
                        throw new NumberFormatException();
                    }
                } else {
                    n = -1;
                    throw new NumberFormatException();
                }
                validInt = (int) n;
                validAnswer = true;
            } catch (NumberFormatException e) {
                if (n == -1) { //invalid type
                    System.out.print("ERROR! You entered an Invalid Type. ");
                } else if (n == 1) { // less than min
                    System.out.print("ERROR! Your number cannot be less than " + min + ". ");
                } else { // more than max
                    System.out.print("ERROR! Your number cannot be greater than " + max + ". ");
                }
                System.out.println("Please enter an integer between " + min + " and " + max + " (inclusive)!");
                n = -1;
                validAnswer = false;
            }
        } while (!validAnswer);

        return validInt;
    }
    public static char getChar(Scanner in, String message) {
        String fullInput = "?"; // stores the original input from the user
        char charInput = '?'; // stores the user's character response
        boolean validAnswer;
        do {
            try {
                System.out.print (message + ": ");
                fullInput = in.nextLine().toLowerCase();

                if (fullInput.length()>1) { // the input is too long (more than one character)
                    fullInput = "1";
                    throw new IOException();
                } else if (fullInput.isEmpty()) { //input length 0
                    fullInput = "2";
                    throw new IOException();
                } else if (!(fullInput.equals("y") || fullInput.equals("n"))) {
                    fullInput = "3";
                    throw new IOException();
                }
                charInput = fullInput.charAt(0);

                validAnswer = true;
            } catch (IOException e) {
                switch (fullInput) {
                    case "1" ->  // input too long
                            System.out.print("ERROR! Your response was more than one character. ");
                    case "2" ->  // input length 0
                            System.out.print("ERROR! You did not provide a response. ");
                    case "3" -> // input is not y or n
                            System.out.print("ERROR! Your response was not 'y' nor was it 'n'. ");
                }
                System.out.println("Please enter a valid character!");
                fullInput = "?";
                validAnswer = false;
            }
        } while (!validAnswer);
        return charInput;
    }
    public static int[] getDate (Scanner in, String message) {
        String inputString ="?"; // stores the input
        boolean validAnswer;
        int[] parsedDate = {};
        do {
            try {
                System.out.print(message + ": ");
                inputString = in.nextLine().trim().toLowerCase();
                if (inputString.isEmpty()) { //input length 0 chars
                    inputString = "1";
                    throw new IOException();
                }
                int firstSlash = inputString.indexOf('/');
                int lastSlash = inputString.lastIndexOf('/');
                if (firstSlash == -1) {
                    inputString = "2";
                    throw new IOException();
                } else if (firstSlash == lastSlash) {
                    inputString = "3";
                    throw new IOException();
                }
                parsedDate = parseDate(inputString);
                if (parsedDate.length == 0) {
                    inputString = "1";
                    throw new NumberFormatException();
                } else if (!Date.validMonthDayYearTriplet(parsedDate)) {
                    inputString = "2";
                    throw new NumberFormatException();
                }

                validAnswer = true;
            } catch (NumberFormatException e) {
                switch (inputString) {
                    case "1" ->  // invalid number
                            System.out.print("ERROR! Your input had invalid characters. ");
                    case "2" ->  // no slashes
                            System.out.print("ERROR! Your date was invalid. ");
                }
                System.out.println("Please enter a date in format MM/DD/YYYY!");
                inputString = "?";
                validAnswer = false;
            } catch (IOException e) {
                switch (inputString) {
                    case "1" ->  // input length 0
                            System.out.print("ERROR! You did not provide a response. ");
                    case "2" ->  // no slashes
                            System.out.print("ERROR! Your input had no slashes. ");
                    case "3" -> // one slash
                            System.out.print("ERROR! Your input only had one slash. ");
                }
                System.out.println("Please enter a date in format MM/DD/YYYY!");
                inputString = "?";
                validAnswer = false;
            }
        } while (!validAnswer);
        return parsedDate;
    }
    public static void importAlbum (Scanner in, ArrayList <Album> albums, boolean binarySearch) {
        boolean validFileName = false;
        while (!validFileName) {
            try {
                System.out.print("Please provide the name of the input file (WITHOUT FILE EXTENSION): ");
                String fileName = in.nextLine();
                BufferedReader inFile = new BufferedReader(new FileReader(fileName + ".txt"));
                validFileName = true;
                System.out.println(readFile(inFile, albums, binarySearch));
                inFile.close();
            } catch (FileNotFoundException e) {
                System.out.println("File Not Found\n");
            } catch (IOException e) {
                System.out.println("Reading error");
            }
        }

    }
    public static String readFile (BufferedReader inFile, ArrayList <Album> albums, boolean binarySearch) {
        try {
            int albumNum;
            if (duplicateAlbum(albumNum = Integer.parseInt(inFile.readLine().trim()),albums,binarySearch)) {
                return "This is a duplicate album";
            }
            Date albumDate = new Date (parseDate(inFile.readLine().trim()));
            int maxCapacity = Integer.parseInt(inFile.readLine().trim());
            int cardsInAlbum = Integer.parseInt(inFile.readLine().trim());
            ArrayList <Card> cards = new ArrayList <> (cardsInAlbum);
            for (int i = 0; i < cardsInAlbum; i++) {
                String name = inFile.readLine().trim();
                int HP = Integer.parseInt(inFile.readLine().trim());
                String type = inFile.readLine().trim();
                Date thisCardDate = new Date (parseDate(inFile.readLine().trim()));
                Attack [] attacks = new Attack [Integer.parseInt(inFile.readLine())];
                for (int j = 0; j < attacks.length; j++) {
                    String attackNameDescription = inFile.readLine().trim();
                    int indexOfHyphen = attackNameDescription.indexOf('-');
                    String attackName, attackDescription;
                    if (indexOfHyphen==-1) { // no hyphen
                        attackName = attackNameDescription;
                        attackDescription = "";
                    } else {
                        attackName = attackNameDescription.substring(0,indexOfHyphen).trim();
                        attackDescription = attackNameDescription.substring(indexOfHyphen+1).trim();
                    }
                    attacks[j] = (new Attack(attackName,attackDescription,inFile.readLine().trim()));
                }
                cards.add(new Card (name, HP, type, thisCardDate,attacks));
            }
            albums.add(new Album(albumNum,cards,maxCapacity,albumDate));
        } catch (IOException e) {
            return "Reading Error";
        }
        return "Album import successful!";
    }
    public static boolean duplicateAlbum (int albumNum, ArrayList <Album> albums, boolean binarySearch) {
        if (binarySearch) {
            return (Collections.binarySearch(albums, new Album (albumNum)) > -1); // true if whole number index
        } else {
            return albums.contains(new Album (albumNum));
        }
    }
    public static int[] parseDate (String date) {
        try {
            int month, day, year;
            int firstSlash = date.indexOf("/");
            int secondSlash = date.lastIndexOf("/");
            month = Integer.parseInt(date.substring(0, firstSlash));
            day = Integer.parseInt(date.substring(firstSlash + 1, secondSlash));
            year = Integer.parseInt(date.substring(secondSlash + 1));
            return new int[]{month, day, year};
        } catch (NumberFormatException e) {
            return new int[]{};
        }
    }
}