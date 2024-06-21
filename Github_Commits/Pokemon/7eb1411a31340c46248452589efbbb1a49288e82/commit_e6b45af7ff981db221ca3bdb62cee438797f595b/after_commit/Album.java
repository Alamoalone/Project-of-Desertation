import java.util.ArrayList;
import java.util.Collections;

public class Album implements Comparable <Album> {
    private final int albumNum;
    private ArrayList <Card> cards;
    private int maxCapacity;
    private Date date;
    private int albumHP;
    private static int totalNumOfCards;
    private static int totalCapacity;
    private static int totalHP;

    public Album (int albumNum, ArrayList <Card> cards, int maxCapacity, Date date) {
        this.albumNum = albumNum;
        this.cards = cards;
        this.maxCapacity = maxCapacity;
        this.date = date;
        for (Card card : cards) {
            this.albumHP += card.getHP();
        }
        totalHP += albumHP;
        totalNumOfCards+=cards.size();
        totalCapacity+=maxCapacity;
    }
    public Album (int albumNum) {
        this.albumNum = albumNum;
    }
    public String averageHP () { // average HP of THIS ALBUM
        return ("Average HP: " + ((double) albumHP /cards.size()));
    }
    public static String averageHPOfCollection () {
        return ("Average HP: " + ((double) totalHP /totalNumOfCards));
    }
    // The total number of cards out of the max capacity of this collection.
    public static String cardsOutOfCapacityCollection () {
        return (totalNumOfCards + " cards out of " + totalCapacity);
    }
    public String cardsOutOfCapacity () { // cards inside this album as compared to max capacity of this album.
        return (cards.size() + " cards out of " + maxCapacity);
    }
    public void printNameDateAllCards () {
        for (int i = 0 ; i < cards.size() ; i++) {
            System.out.println((i+1) + ": ");
            System.out.println(cards.get(i).nameDateToString() + "\n");
        }
    }
    public void printAllInfoAllCards () {
        for (Card card : cards) {
            System.out.println(card + "\n");
        }
    }
    public ArrayList <Card> getCards() {
        return cards;
    }
    public void addCard (Card c) {
        cards.add(c);
        albumHP += c.getHP();
        totalHP += albumHP;
        totalNumOfCards++;
        Collections.sort(cards);
    }
    public void sortCardsByName () {
        Collections.sort(cards);
    }
    public void sortCardsByHP () {
        cards.sort(new SortByHP());
    }
    public void sortCardsByDate () {
        cards.sort(new SortByDate());
    }
    public void removeCard (int index) {
        cards.remove(index);
    }
    public void removeCards (int startIndex, int endIndex) {
        cards.subList(startIndex, endIndex).clear();
    }
    public int getCardOfName (String name, boolean firstCard) {
        if (firstCard) {
            return cards.indexOf(new Card(name));
        } else {
            return cards.lastIndexOf(new Card(name));
        }

    }
    public Card getCard (int index) {
        return cards.get(index);
    }
    public int compareTo (Album a) {
        return (this.albumNum - a.albumNum);
    }
    public String toString () {
        return String.format("Album Number: %d%n" +
                "Date: %s%n" +
                "Max Capacity: %d%n" +
                "Number of Cards: %d%n" +
                "Total HP: %d%n", albumNum, date, maxCapacity, cards.size(), albumHP);
    }
    public int getCardsSize () {
        return cards.size();
    }
    public boolean atMaxCapacity () {
        return cards.size() == maxCapacity;
    }
    public int getCardOfHP (int hp, boolean firstCard) {
        if (firstCard) {
            return cards.indexOf(new Card(hp));
        } else {
            return cards.lastIndexOf(new Card(hp));
        }
    }
    public String nameDateToString () {
        return String.format ("Album Number: %d%nDate: %s%n",albumNum,date);
    }
}
