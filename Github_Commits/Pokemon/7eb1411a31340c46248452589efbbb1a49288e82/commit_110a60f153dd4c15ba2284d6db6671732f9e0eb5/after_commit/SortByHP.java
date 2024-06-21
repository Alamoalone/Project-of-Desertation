import java.util.Comparator;

public class SortByHP implements Comparator <Card> {
    public int compare (Card a, Card b) {
        int hpDifference;
        if ((hpDifference = a.getHP()-b.getHP()) == 0) {// same hp
            int nameDifference;
            if ((nameDifference = a.getName().compareToIgnoreCase(b.getName())) == 0) { // same name
                return a.getName().compareToIgnoreCase(b.getName());
            } else {
                return nameDifference;
            }
        } else {
            return hpDifference;
        }
    }
}