package range_speed;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class comparatorStr implements Comparator<String> {
    @Override
    public int compare(String o1, String o2) {
        double d1 = Double.parseDouble(o1);
        double d2 = Double.parseDouble(o2);
        if (d1 == d2) {
            return 0;
        } else if (d1 > d2) {
            return 1;
        } else {
            return -1;
        }
    }
}


class comparatorDate implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        Date date1,date2;
        try {
            date1=new SimpleDateFormat("MM/dd/yyyy").parse(o1);
            date2=new SimpleDateFormat("MM/dd/yyyy").parse(o2);
            return date1.compareTo(date2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
