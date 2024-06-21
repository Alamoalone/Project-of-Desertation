import java.util.Comparator;

public class SortByDate implements Comparator <Card> {
    public int compare (Card a, Card b) {
        if (a.getDate() == null || b.getDate() == null) {
            return 0;
        }
        int yearDifference;
        if ((yearDifference = a.getDate().getYear()-b.getDate().getYear())==0) {
            int monthDifference;
            if ((monthDifference = a.getDate().getMonth()-b.getDate().getMonth())==0) {
                int dayDifference;
                if ((dayDifference = a.getDate().getDay()-b.getDate().getDay())==0) {
                    return a.getName().compareToIgnoreCase(b.getName());
                } else {
                    return dayDifference;
                }
            } else {
                return monthDifference;
            }
        } else {
            return yearDifference;
        }
    }
}
