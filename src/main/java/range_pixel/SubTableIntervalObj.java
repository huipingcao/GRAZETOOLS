package range_pixel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SubTableIntervalObj {
    public Date start_date;
    public Date end_date;
    int id;
    ArrayList<DaysIntervalObj> daysintervalobjList = new ArrayList<>();
    SimpleDateFormat date_formatter = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public String toString() {
        return "Sub Table Interval " + id +
                " : start_date=" + date_formatter.format(start_date) +
                ", end_date=" + date_formatter.format(end_date);
    }
}
