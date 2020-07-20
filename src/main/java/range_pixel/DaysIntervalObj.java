package range_pixel;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DaysIntervalObj {
    public int id;
    public Date start_date;
    public Date end_date;

    SimpleDateFormat date_formatter = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public String toString() {
        return "Days Interval " + id +
                " : start_date=" + date_formatter.format(start_date) +
                ", end_date=" + date_formatter.format(end_date);
    }
}
