package range_pixel;

import org.apache.commons.lang3.tuple.Pair;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class myComparator {
}

class SortByTime implements Comparator<Pair<String, double[]>> {
    DateFormat TimeFormatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");

    public int compare(Pair<String, double[]> n1, Pair<String, double[]> n2) {
        Date d1 = null;
        Date d2 = null;
        try {
            d1 = this.TimeFormatter.parse(n1.getKey());
            d2 = this.TimeFormatter.parse(n2.getKey());


            if (d1.before(d2)) {
                return -1;
            } else if (d1.after(d2)) {
                return 1;
            } else {
                return 0;
            }
        } catch (ParseException e) {
            System.out.println("There is something wrong with your time formation, please check it 1");
            System.exit(0);
        }
        return 0;

    }
}


class SortByDate implements Comparator<String> {
    DateFormat TimeFormatter = new SimpleDateFormat("yyyy-MM-dd");

    public int compare(String n1, String n2) {
        Date d1 = null;
        Date d2 = null;
        try {
            d1 = this.TimeFormatter.parse(n1);
            d2 = this.TimeFormatter.parse(n2);


            if (d1.before(d2)) {
                return -1;
            } else if (d1.after(d2)) {
                return 1;
            } else {
                return 0;
            }
        } catch (ParseException e) {
            System.out.println("There is something wrong with your time formation, please check it 2");
            System.exit(0);
        }
        return 0;

    }
}

class SortByCowid implements Comparator<String> {

    public int compare(String n1, String n2) {
        if (Integer.valueOf(n1) < Integer.valueOf(n2)) {
            return -1;
        } else if (Integer.valueOf(n1) > Integer.valueOf(n2)) {
            return 1;
        }
        return 0;

    }
}
