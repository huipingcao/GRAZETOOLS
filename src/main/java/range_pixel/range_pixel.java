package range_pixel;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Given the pixel file that contains the range geographic information and the GPS records, it calculate the percent grazed pixels, and pixel residence time, revisit rate (visits on different days), and return interval (interval between visits when cows visited the same pixel for more than once) by considering the velocity of the cow. The cow velocity is calculated by two consecutive gps records.
 * Logic: The velocity is computed by two consecutive GPS records as well. If the speed of the cow is between given thresholds min_speed and max_speed, we think the cow revisited back to one pixel. Otherwise, we think the cow just ran through or rested one pixel.
 * Input: file name of coordination data, file name of pixel data, min speed and max speed, visit interval, interval of the sub table
 * Output: result1.csv - For each cow and each pixel calculate how many intervals this cow visit back to this pixel
 * result2.csv - For each year, each cow and each pixel calculate how many days this cow visit back to this pixel
 * result3.csv - For each year, each cow and each pixel calculate the average of the number of days this cow visit back to this pixel
 * result4.csv - The number of times and time spent of each cow in each pixel
 * result5.csv - The number of intervals of each cow in each pixel
 * result6.csv - The number of pixels that is visited by cows with different counts in one interval
 */
public class range_pixel {

    //    String GPSData = "data/pixel/sample_gps.csv";
    String pixelPath = "pixel.csv";
    String GPSData = "coordinate_pointscsv";

//    String pixelPath = "data/pixel/pixel.csv";
//    String GPSData = "data/pixel/coordinate_points.csv";


    HashMap<Long, Pair<Double, Double>> pixelList = new HashMap<Long, Pair<Double, Double>>(); //pixel_id -> <northing,easting>
    HashMap<String, HashMap<Long, HashSet<String>>> result = new HashMap<>(); //cowID—> Hashmap<pixel_id,date list that the cow was in the pixel>
    HashMap<String, HashMap<Long, Pair<Integer, Double>>> visited_result = new HashMap<>(); //cowID—> Hashmap<pixel_id, <number of times and time in mins that the cow was in the pixel>
    HashMap<String, HashMap<Long, HashMap<Long, HashSet<String>>>> yearInfos = new HashMap<>(); //cowID—> HashMap<year --> HashMap<pixel_id,date list that the cow was in the pixel>>

    ArrayList<DaysIntervalObj> days_interval_mapping_list = new ArrayList<>();
    ArrayList<SubTableIntervalObj> sub_table_mapping_list = new ArrayList<>();

    //accepted parameters
    private int range_size = 30; //the size of the each pixel square
    private double min_speed = 5;
    private double max_speed = 100;
    private int day_interval = 1;
    private int sub_table_interval = 30;

    private HashMap<Long, String> pixel_extra_info = new HashMap<>();//pix
    private String extra_title = "";

    SimpleDateFormat date_formatter = new SimpleDateFormat("yyyy-MM-dd");


    public static void main(String args[]) {
        range_pixel rp = new range_pixel();
        rp.readTheFileName();
        rp.loadPixelData();
        rp.loadGPSDateInformation();
//        System.out.println(rp.pixelList.size());
        rp.loadGPSData();
//        System.out.println(rp.result.size());
//        rp.printVisitDateWithCowIDandPid("953","113");

        System.out.println("=======================================================================================");
        rp.printResult1();
        rp.setYearinfos();
        rp.printResult2();
        rp.printResult3();
        rp.printResult4();
        rp.printResult5();
        rp.printResult6();
    }


    /**
     * Get file name of coordination data, file name of pixel data, min speed and max speed, visit interval, interval of the sub table from user
     */
    private void readTheFileName() {
        InputStreamReader inp = new InputStreamReader(System.in);
        BufferedReader in = new BufferedReader(inp);
        String str = null;
        try {
            System.out.println("Enter cordination file name (Default: coordinate_points.csv): ");
            str = in.readLine();
            if (str.trim().length() > 0) {
                this.pixelPath = str;
            }
            //process weather file names
            System.out.println("Enter Pixel file name (Default: pixel.csv) : ");
            str = in.readLine();
            if (str.trim().length() > 0) {
                this.GPSData = str;
            }

            System.out.println("Enter meters of the range (Default: 30) : ");
            str = in.readLine();
            if (str.trim().length() > 0) {
                this.range_size = Integer.parseInt(str);
            }

            System.out.println("Enter min speed (Default: 5.0) : ");
            str = in.readLine();
            if (str.trim().length() > 0) {
                this.min_speed = Double.parseDouble(str);
            }

            System.out.println("Enter max speed (Default: 100.0) : ");
            str = in.readLine();
            if (str.trim().length() > 0) {
                this.max_speed = Double.parseDouble(str);
            }

            System.out.println("Enter visit interval (Default: 1 day) : ");
            str = in.readLine();
            if (str.trim().length() > 0) {
                this.day_interval = Integer.parseInt(str);
            }

            System.out.println("Enter days range for the sub-tables of visits (Default: 30 days) : ");
            str = in.readLine();
            if (str.trim().length() > 0) {
                this.sub_table_interval = Integer.parseInt(str);
            }

        } catch (IOException e) {
            System.err.println("There is something wrong with your input of the file name, please check it.");
        }

    }


    /**
     * Divide data into years
     */
    private void setYearinfos() {
        for (Map.Entry<String, HashMap<Long, HashSet<String>>> cow_infos : this.result.entrySet()) {
            String cowid = cow_infos.getKey();
            for (Map.Entry<Long, HashSet<String>> pixel_infos : cow_infos.getValue().entrySet()) {
                long pixel_id = pixel_infos.getKey();
                Calendar c = Calendar.getInstance();
                SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");

                for (String dateStr : pixel_infos.getValue()) {
                    try {
                        c.setTime(ft.parse(dateStr));
                        long year = c.get(Calendar.YEAR);

                        HashMap<Long, HashMap<Long, HashSet<String>>> years = yearInfos.get(cowid);
                        if (years == null) {
                            years = new HashMap<>();
                        }


                        HashMap<Long, HashSet<String>> pixels = years.get(year);
                        if (pixels == null) {
                            pixels = new HashMap<>();
                        }

                        HashSet<String> dateList = pixels.get(pixel_id);
                        if (dateList == null) {
                            dateList = new HashSet<>();
                        }

                        dateList.add(dateStr);
                        pixels.put(pixel_id, dateList);
                        years.put(year, pixels);
                        yearInfos.put(cowid, years);


                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
//        System.out.println(yearInfos.size());
    }


    /**
     * Calculate and write the statistics of the number of pixels that is visited by cows with different counts(e.g. how many pixels are visited only once/twice/three times, and etc) in one interval into result6.csv
     * pixel_visit_summary - this pixel visits by cows statistics
     * Logic: pixel_visit_summary = <cow_id, visit_info>
     * this.result cowID—> Hashmap<pixel_id,date list that the cow was in the pixel>
     */
    private void printResult6() {
        File file = new File("result6.csv");

        if (file.exists()) {
            file.delete();
        }

        try (FileWriter fw = new FileWriter(file, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            StringBuffer sb_title = new StringBuffer();
            sb_title.append("# number of visits ,");

            for (String cow_id : this.result.keySet()) {
                sb_title.append(cow_id).append(",");
            }

            sb_title.append("all");
            out.println(sb_title);

            HashMap<String, HashMap<Integer, Integer>> pixel_visit_summary = new HashMap<>();
            HashSet<Integer> distinc_visit_number = new HashSet<>();

            for (long pixel_id : pixelList.keySet()) {

                HashSet<String> date_list_in_pixel_all = new HashSet<>();

                for (String cow_id : this.result.keySet()) {
                    int size = 0;

                    HashSet<String> date_list_in_pixel = this.result.get(cow_id).get(pixel_id);

                    if (date_list_in_pixel != null) {
                        date_list_in_pixel_all.addAll(date_list_in_pixel);
                        size = getSizeInDaysInterval(date_list_in_pixel);
                    }

                    if (size != 0) {

                        if (!distinc_visit_number.contains(size)) {
                            distinc_visit_number.add(size);
                        }

                        if (pixel_visit_summary.containsKey(cow_id)) {
                            HashMap<Integer, Integer> visit_info = pixel_visit_summary.get(cow_id);
                            if (visit_info.containsKey(size)) {
                                visit_info.put(size, visit_info.get(size) + 1);
                            } else {
                                visit_info.put(size, 1);
                            }
                            pixel_visit_summary.put(cow_id, visit_info);

                        } else {
                            HashMap<Integer, Integer> visit_info = new HashMap<>();
                            visit_info.put(size, 1);
                            pixel_visit_summary.put(cow_id, visit_info);
                        }
                    }
                }


                int size = 0;

                if (date_list_in_pixel_all != null) {
                    size = getSizeInDaysInterval(date_list_in_pixel_all);
                }

                if (size != 0) {

                    if (!distinc_visit_number.contains(size)) {
                        distinc_visit_number.add(size);
                    }

                    String cow_id_all = "all";
                    if (pixel_visit_summary.containsKey(cow_id_all)) {
                        HashMap<Integer, Integer> visit_info = pixel_visit_summary.get(cow_id_all);
                        if (visit_info.containsKey(size)) {
                            visit_info.put(size, visit_info.get(size) + 1);
                        } else {
                            visit_info.put(size, 1);
                        }
                        pixel_visit_summary.put(cow_id_all, visit_info);

                    } else {
                        HashMap<Integer, Integer> visit_info = new HashMap<>();
                        visit_info.put(size, 1);
                        pixel_visit_summary.put(cow_id_all, visit_info);
                    }
                }
            }

            ArrayList<Integer> sorted_keys = new ArrayList<>(distinc_visit_number);
            Collections.sort(sorted_keys);


            for (int key : sorted_keys) {
                StringBuffer sb_row = new StringBuffer();
                sb_row.append(key).append(",");

                for (String cow_id : this.result.keySet()) {
                    int visits = pixel_visit_summary.get(cow_id).get(key) == null ? 0 : pixel_visit_summary.get(cow_id).get(key);
                    sb_row.append(visits).append(",");
                }

                int visits = pixel_visit_summary.get("all").get(key) == null ? 0 : pixel_visit_summary.get("all").get(key);
                sb_row.append(visits);
                out.println(sb_row);

            }

            out.close();
            System.out.println("Done!! See result6.csv,  pixel-cowid visits summary.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * Calculate and write the statistics of the number of intervals of each cow in each pixel. It consist of one master table and several sub-tables into result5.csv file.
     * Each row is the number of visited intervals of each cows.
     */
    private void printResult5() {
        File file = new File("result5.csv");

        if (file.exists()) {
            file.delete();
        }

        try (FileWriter fw = new FileWriter("result5.csv", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            StringBuffer sb_title = new StringBuffer();
            sb_title.append(" ,");

            for (String cow_id : this.result.keySet()) {
                sb_title.append(cow_id).append(",");
            }

            String str_title = sb_title.toString();
            out.println(str_title.substring(0, str_title.lastIndexOf(",")));

            for (long pixel_id : pixelList.keySet()) {
                StringBuffer master_table_row = new StringBuffer();
                master_table_row.append(pixel_id).append(",");

                for (String cow_id : this.result.keySet()) {
                    int size = 0;
                    HashSet<String> date_list_in_pixel = this.result.get(cow_id).get(pixel_id);
                    if (date_list_in_pixel != null) {
                        size = getSizeInDaysInterval(date_list_in_pixel);
                    }
                    master_table_row.append(size).append(",");
                }
                String master_str_row = master_table_row.toString();
                out.println(master_str_row.substring(0, master_str_row.lastIndexOf(",")));
            }
            out.close();
            System.out.println("Done!! See result5.csv,  pixel-cowid visits mappings.");
        } catch (IOException e) {
            e.printStackTrace();
        }


        createSubtables();


    }


    /**
     * Generate sub-tables of result5. The each sub-table is shows the statistic over the date in one sub-table interval which is controlled by the parameter - interval of the sub table.
     */
    private void createSubtables() {
        for (SubTableIntervalObj sub_tabl_object : sub_table_mapping_list) {

            SimpleDateFormat df = new SimpleDateFormat("ddMMyyyy");
            String start_date = df.format(sub_tabl_object.start_date);
            String end_date = df.format(sub_tabl_object.end_date);

            File file = new File("result5_subtable_" + start_date + "_" + end_date + ".csv");

            if (file.exists()) {
                file.delete();
            }

            try (FileWriter fw = new FileWriter(file, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {

                StringBuffer sb_title = new StringBuffer();
                sb_title.append(" ,");

                for (String cow_id : this.result.keySet()) {
                    sb_title.append(cow_id).append(",");
                }

                String str_title = sb_title.toString();
                out.println(str_title.substring(0, str_title.lastIndexOf(",")));

                for (long pixel_id : pixelList.keySet()) {
                    StringBuffer master_table_row = new StringBuffer();
                    master_table_row.append(pixel_id).append(",");

                    for (String cow_id : this.result.keySet()) {
                        int size = 0;
                        HashSet<String> date_list_in_pixel = this.result.get(cow_id).get(pixel_id);
                        if (date_list_in_pixel != null) {
                            size = getSizeInSubTableInterval(date_list_in_pixel, sub_tabl_object.daysintervalobjList);
                        }
                        master_table_row.append(size).append(",");
                    }
                    String master_str_row = master_table_row.toString();
                    out.println(master_str_row.substring(0, master_str_row.lastIndexOf(",")));
                }
                out.close();
                System.out.println("   |--- Done!! See result5 sub-tables,  pixel-cowid visits mappings. ( " + file.getName() + " )");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    /**
     * Calculate and write the statistics of the number of times and time spent of each cow in each pixel into result4.csv file
     * this.visited_result cowID—> Hashmap<pixel_id, <number of times and time in mins that the cow was in the pixel>
     */
    private void printResult4() {
        File file = new File("result4.csv");

        if (file.exists()) {
            file.delete();
        }

        try (FileWriter fw = new FileWriter("result4.csv", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println("Cow_id,Pixel ID,Number of visited times,time spent," + this.extra_title + "Northing,Easting");
            for (Map.Entry<String, HashMap<Long, Pair<Integer, Double>>> cow_infos : this.visited_result.entrySet()) {
                String cowid = cow_infos.getKey();
                for (Map.Entry<Long, Pair<Integer, Double>> pixel_infos : cow_infos.getValue().entrySet()) {
                    long pixel_id = pixel_infos.getKey();
                    int times = pixel_infos.getValue().getKey();
                    String spent = String.format("%.4f", pixel_infos.getValue().getValue());
                    String pixelVegClass = this.pixel_extra_info.get(pixel_id);
                    out.println(cowid + "," + pixel_id + "," + times + "," + spent + "," + pixelVegClass
                            + this.pixelList.get(pixel_id).getKey() + "," + this.pixelList.get(pixel_id).getValue());
                }
            }

            out.close();

            System.out.println("Done!! See result4.csv,  how much time each animal spends in each cell (in minutes).");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * Calculate and write the statistics that for each year, each cow and each pixel calculate the average of the number of days this cow visit back to this pixel into result3.csv file
     */
    private void printResult3() {
        File file = new File("result3.csv");

        if (file.exists()) {
            file.delete();
        }

        try (FileWriter fw = new FileWriter("result3.csv", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {


            out.println("Cow_id, Years, Number_of_Pixels, average days the cow visited in each pixel in this year, average interval days the cow visited back in each pixel");
            for (Map.Entry<String, HashMap<Long, HashMap<Long, HashSet<String>>>> cow_infos : yearInfos.entrySet()) {
                String cowid = cow_infos.getKey();
                HashMap<Long, ArrayList<Double>> yearsCounts = new HashMap<>();
                for (Map.Entry<Long, HashMap<Long, HashSet<String>>> years_obj : cow_infos.getValue().entrySet()) {
                    long year = years_obj.getKey();
                    for (Map.Entry<Long, HashSet<String>> pixel_infos : years_obj.getValue().entrySet()) {
                        long pixel_id = pixel_infos.getKey();

                        //int size = pixel_infos.getValue().size();
                        int size = getSizeInDaysInterval(pixel_infos.getValue());

                        long diff = getDifferDate(pixel_infos.getValue());
                        double period = size - 1 != 0 ? (double) diff / (size - 1) : 0;

                        if (period != 0) {
                            ArrayList<Double> year_obj = yearsCounts.get(year);
                            if (year_obj == null) {
                                year_obj = new ArrayList<>();
                                year_obj.add(0, 1.0);
                                year_obj.add(1, (double) size);
                                year_obj.add(2, period);
                            } else {
                                year_obj.set(0, year_obj.get(0) + 1);
                                year_obj.set(1, year_obj.get(1) + size);
                                year_obj.set(2, year_obj.get(2) + period);
                            }
                            yearsCounts.put(year, year_obj);
                        }
                    }
                }

                TreeSet<Long> keyList = new TreeSet<>(yearsCounts.keySet());
                for (long key : keyList) {
                    int size = yearsCounts.get(key).get(0).intValue();
                    double avg_total = yearsCounts.get(key).get(1) / size;
                    double avg_period = yearsCounts.get(key).get(2) / size;
                    out.println(cowid + "," + key + "," + size + "," + avg_total + "," + avg_period);
                }

            }
            out.close();

            System.out.println("Done!! See result3.csv, for each year, each cow and each pixel calculate the average of the number of days this cow visit back to this pixel.");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * Calculate and write the statistics that for each year, each cow and each pixel calculate how many days this cow visit back to this pixel into result2.csv file
     */
    private void printResult2() {
        File file = new File("result2.csv");

        if (file.exists()) {
            file.delete();
        }
        try (FileWriter fw = new FileWriter("result2.csv", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {


            out.println("Cow_id, Years, Pixel_id, times of visiting back to pixel, period value," + this.extra_title + "Northing,Easting");

            for (Map.Entry<String, HashMap<Long, HashMap<Long, HashSet<String>>>> cow_infos : yearInfos.entrySet()) { //cow_id
                String cowid = cow_infos.getKey();
                for (Map.Entry<Long, HashMap<Long, HashSet<String>>> years_obj : cow_infos.getValue().entrySet()) { //year_id
                    long year = years_obj.getKey();
                    for (Map.Entry<Long, HashSet<String>> pixel_infos : years_obj.getValue().entrySet()) { //pixel ==> list of date
                        long pixel_id = pixel_infos.getKey();

                        //                        int size = pixel_infos.getValue().size();
                        int size = getSizeInDaysInterval(pixel_infos.getValue());

                        long diff = getDifferDate(pixel_infos.getValue());
                        String period = size - 1 != 0 ? String.valueOf((double) diff / (size - 1)) : "\\N";
//                        out.println(cowid + "," + year + "," + pixel_id + "," + size + "," + period);
                        String pixelVegClass = pixel_extra_info.get(pixel_id);
                        out.println(cowid + "," + year + "," + pixel_id + "," + size + "," + period + "," + pixelVegClass
                                + this.pixelList.get(pixel_id).getKey() + "," + this.pixelList.get(pixel_id).getValue());

                    }
                }

            }
            out.close();

            System.out.println("Done!! See result2.csv, for each year, each cow and each pixel calculate how many days this cow visit back to this pixel.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Calculate and write the statistics. For each cow and each pixel calculate how many intervals this cow visit back to this pixel into result1. csv file.
     * The interval is controlled by the parameter visit interval.(No matter how many times this cow visited this pixel within the interval days, only counts 1)
     */
    private void printResult1() {
        File file = new File("result1.csv");

        if (file.exists()) {
            file.delete();
        }

        try (FileWriter fw = new FileWriter(file.getName(), true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println("Cow_id, Pixel_id, times of visiting back to pixel, Interval days,visited back dates list," + this.extra_title + "Northing,Easting");

            for (Map.Entry<String, HashMap<Long, HashSet<String>>> cow_infos : this.result.entrySet()) { //cow_id
                String cowid = cow_infos.getKey();
                for (Map.Entry<Long, HashSet<String>> pixel_infos : cow_infos.getValue().entrySet()) { //pixel_id --> list of dates
                    long pixel_id = pixel_infos.getKey();

//                    int size = pixel_infos.getValue().size();
                    int size = getSizeInDaysInterval(pixel_infos.getValue());
                    long diff = getDifferDate(pixel_infos.getValue());
                    String date_list = getDateList(pixel_infos.getValue());
//                    out.println(cowid + "," + pixel_id + "," + size + "," + diff);

                    String pixelVegClass = this.pixel_extra_info.get(pixel_id);
                    out.println(cowid + "," + pixel_id + "," + size + "," + diff + "," + date_list + "," + pixelVegClass
                            + this.pixelList.get(pixel_id).getKey() + "," + this.pixelList.get(pixel_id).getValue());
                }
            }

            out.close();
            System.out.println("Done!! See result1.csv, for each cow and each pixel calculate how many days this cow visit back to this pixel.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Get how many intervals in cows visit records
     *
     * @param datelist
     * @return the size
     */
    private int getSizeInDaysInterval(HashSet<String> datelist) {
        HashSet<DaysIntervalObj> visited_interval_list = new HashSet();

        try {
            for (String str_date : datelist) {
                for (DaysIntervalObj d_interval_obj : this.days_interval_mapping_list) {
                    Date d = date_formatter.parse(str_date);
                    if (d.compareTo(d_interval_obj.start_date) >= 0 && d.compareTo(d_interval_obj.end_date) <= 0) {
                        visited_interval_list.add(d_interval_obj);
                        continue;
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return visited_interval_list.size();
    }


    /**
     * Get Get how many intervals in sub-tables of cows visit records
     *
     * @param datelist
     * @param daysintervalobjList
     * @return
     */
    private int getSizeInSubTableInterval(HashSet<String> datelist, ArrayList<DaysIntervalObj> daysintervalobjList) {
        HashSet<DaysIntervalObj> visited_interval_list = new HashSet();

        try {
            for (String str_date : datelist) {
                for (DaysIntervalObj d_interval_obj : daysintervalobjList) {
                    Date d = date_formatter.parse(str_date);
                    if (d.compareTo(d_interval_obj.start_date) >= 0 && d.compareTo(d_interval_obj.end_date) <= 0) {
                        visited_interval_list.add(d_interval_obj);
                        continue;
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return visited_interval_list.size();
    }

    /**
     * @param dateList the list of the dates
     * @return the interval days of max date minus the min date in the given datas
     */
    private long getDifferDate(HashSet<String> dateList) {
        Date mindate = null, maxdate = null;
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
        for (String date : dateList) {
            try {
                Date tempdate = ft.parse(date);
                if (maxdate == null || tempdate.compareTo(maxdate) > 0) {
                    maxdate = tempdate;
                }

                if (mindate == null || tempdate.compareTo(mindate) < 0) {
                    mindate = tempdate;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
//        System.out.println((maxdate.getTime() - mindate.getTime()));
//        System.out.println((maxdate.getTime() - mindate.getTime()) / (1000*60*60*24));
//        System.out.println((double)(maxdate.getTime() - mindate.getTime()) / (1000*60*60*24));
        return Math.round((double) (maxdate.getTime() - mindate.getTime()) / (1000 * 60 * 60 * 24));

    }

    /**
     * @param pre_time     previous time in HH:mm:ss format
     * @param current_time current time in HH:mm:ss format
     * @param date         current date in yyyy-MM-dd format
     * @return the difference in min
     */
    private double getDifferTime(String pre_time, String current_time, String date) {
        double differ = 0.0;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d1 = formatter.parse(date + " " + pre_time);
            Date d2 = formatter.parse(date + " " + current_time);

            long differ_in_sec = (d2.getTime() - d1.getTime()) / 1000;
            differ = differ_in_sec / 60.0;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return differ;
    }

    /**
     * @param pre_date     previous date in yyyy-MM-dd format
     * @param pre_time     previous time in HH:mm:ss format
     * @param current_date current date in yyyy-MM-dd format
     * @param current_time current time in HH:mm:ss format
     * @return the difference in min
     */
    private double getDifferTime(String pre_date, String pre_time, String current_date, String current_time) {
        double differ = 0.0;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d1 = formatter.parse(pre_date + " " + pre_time);
            Date d2 = formatter.parse(current_date + " " + current_time);

            long differ_in_sec = (d2.getTime() - d1.getTime()) / 1000;
            differ = differ_in_sec / 60.0;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return differ;
    }

    /**
     * @param datesList
     * @return formated date lists
     */
    private String getDateList(HashSet<String> datesList) {
        TreeSet<String> sortedList = new TreeSet<>(new SortByDate());
        sortedList.addAll(datesList);

        String sr = "";
        Iterator<String> i = sortedList.iterator();
        while (i.hasNext())
            sr += i.next() + ";";
        return sr.substring(0, sr.lastIndexOf(";"));
    }

    /**
     * print Visit Date With CowID and Pid
     *
     * @param cowid
     * @param pid
     */
    private void printVisitDateWithCowIDandPid(String cowid, String pid) {
        long Lpid = Long.parseLong(pid);
        HashSet<String> dataList = this.result.get(cowid).get(Lpid);
//        System.out.println(dataList.size());
        for (String d : dataList) {
            System.out.println(d);
        }
        System.out.println(getDifferDate(dataList));
    }

    /**
     * print Pixel List With CowID
     *
     * @param cowid
     */
    private void printPixelListWihtCowID(String cowid) {
        System.out.println("==========");
        HashMap<Long, HashSet<String>> pList = this.result.get(cowid);
        System.out.println(cowid + "  " + pixelList.keySet().size());
        for (Long key : pList.keySet()) {
            HashSet<String> datelist = pList.get(key);
            System.out.println("    " + key);
            for (String dt : datelist) {
                System.out.println("        " + dt);
            }
        }
    }

    /**
     * Load pixel data and store info in this.pixelList and this.pixel_extra_info
     */
    private void loadPixelData() {
        BufferedReader br = null;
        int linenumber = 0;

        try {
            br = new BufferedReader(new FileReader(this.pixelPath));
            String line = null;
            while ((line = br.readLine()) != null) {
                linenumber++;
                //jump the header
                if (linenumber == 1) {
                    //get extra title
                    String[] infos = line.split(",");
                    if (infos.length > 3) {
                        int i = 3;
                        for (; i < infos.length; i++) {
                            this.extra_title += (infos[i] + ",");
                        }
                    }
                    continue;
                }

                String[] infos = line.split(",");
                Long pixelID = Long.parseLong(infos[0]);
                Double pixelNorthing = Double.parseDouble(infos[1]);
                Double pixelEasting = Double.parseDouble(infos[2]);

                //get extra information values
                String extraInfo = "";
                if (infos.length > 3) {
                    int i = 3;
                    for (; i < infos.length; i++) {
                        extraInfo += (infos[i] + ",");
                    }
                }


                this.pixelList.put(pixelID, new MutablePair<>(pixelNorthing, pixelEasting));
                this.pixel_extra_info.put(pixelID, extraInfo);
//                System.out.println(pixelID+","+pixelNorthing+","+pixelEasting+",");
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Can not open the Pixel file, please check it. ");
        }
        System.out.println("read the pixel file done" + "   " + linenumber);
//        System.exit(0);
//        System.out.println(this.pixelList.size());
//        System.out.println(this.pixel_veg_class.size());

    }


    /**
     * Load GPS data and store info in days_interval_mapping_list
     */
    private void loadGPSDateInformation() {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = null;
        int linenumber = 0;


        try {

            Date min_d = date_formatter.parse("5000-12-31"); //assume minimal data by using the java calendar object
            Date max_d = date_formatter.parse("1900-01-01"); //assume maximal data by using the java calendar object

            br = new BufferedReader(new FileReader(this.GPSData));
            String line = null;
            while ((line = br.readLine()) != null) {
                linenumber++;
                //jump the header
                if (linenumber == 1) {
                    continue;
                }

                String str_date = line.split(",")[2];
                Date d = date_formatter.parse(str_date);
                if (d.before(min_d)) {
                    min_d = d;
                }

                if (d.after(max_d)) {
                    max_d = d;
                }
            }

            System.out.println("=======================================================================================");
            System.out.println("Min date in the GPS records is " + date_formatter.format(min_d.getTime()));
            System.out.println("Max date in the GPS records is " + date_formatter.format(max_d.getTime()));

            buildTheIntervalMapping(min_d, max_d);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            System.out.println("There is the some issue with the format of the date or time in the coordinate file. ");
            e.printStackTrace();
        }
//        System.exit(0);
    }

    /**
     * Helper function for building the interval mapping according to the minimum date and the maximum date in the visit records
     *
     * @param min_d
     * @param max_d
     */
    private void buildTheIntervalMapping(Date min_d, Date max_d) {
        Date current_d = min_d;
        int interval = 0;
        int interval_id = 0;

        DaysIntervalObj intervalObj = new DaysIntervalObj();
        intervalObj.id = interval_id;
        intervalObj.start_date = min_d;
        days_interval_mapping_list.add(intervalObj);


        while (current_d.compareTo(max_d) != 0) {
//            System.out.println(date_formatter.format(current_d) + "   " + interval + "  " + interval_id);
            intervalObj.end_date = current_d;

            interval++;


            Calendar cal = Calendar.getInstance();
            cal.setTime(current_d);
            cal.add(Calendar.DATE, 1); //minus number would decrement the days
            current_d = cal.getTime();

            if (interval == day_interval) {
                interval = 0;
                interval_id++;

                intervalObj = new DaysIntervalObj();
                intervalObj.id = interval_id;
                intervalObj.start_date = current_d;
                days_interval_mapping_list.add(intervalObj);

            }
        }

//        System.out.println(date_formatter.format(current_d) + "   " + interval + "  " + interval_id);
        intervalObj.end_date = current_d;
        System.out.println("=======================================================================================");
        System.out.println("The days intervel is " + this.day_interval + " (day / days)");
        for (DaysIntervalObj e : days_interval_mapping_list) {
            System.out.println(e);
        }


        SubTableIntervalObj s_table_obj = new SubTableIntervalObj();
        int s_id = 0;
        Calendar cal = Calendar.getInstance();
        cal.setTime(min_d);
        cal.add(Calendar.DATE, this.sub_table_interval); //minus number would decrement the days
        current_d = cal.getTime();
        s_table_obj.start_date = min_d;
        s_table_obj.end_date = current_d;
        s_table_obj.id = s_id;
        sub_table_mapping_list.add(s_table_obj);

        while (current_d.compareTo(max_d) < -1) {

            s_table_obj = new SubTableIntervalObj();
            s_id++;
            s_table_obj.start_date = current_d;
            s_table_obj.id = s_id;

            cal = Calendar.getInstance();
            cal.setTime(current_d);
            cal.add(Calendar.DATE, this.sub_table_interval); //minus number would decrement the days
            current_d = cal.getTime();
            s_table_obj.end_date = current_d;
            sub_table_mapping_list.add(s_table_obj);
        }


        System.out.println("=======================================================================================");
        System.out.println("The sub table interval is " + this.sub_table_interval + " (day / days)");
        for (SubTableIntervalObj s_obj : sub_table_mapping_list) {
            for (DaysIntervalObj d_obj : days_interval_mapping_list) {
                if (d_obj.start_date.compareTo(s_obj.start_date) >= 0 && d_obj.end_date.compareTo(s_obj.end_date) <= 0) {
                    s_obj.daysintervalobjList.add(d_obj);
                } else if (d_obj.start_date.compareTo(s_obj.start_date) >= 0 && d_obj.start_date.compareTo(s_obj.end_date) < 0 && d_obj.end_date.compareTo(s_obj.end_date) > 0) {
                    DaysIntervalObj intervalObj_end = new DaysIntervalObj(); //the d_obj from d_obj.start_date to s_obj.end_date
                    intervalObj_end.start_date = d_obj.start_date;
                    intervalObj_end.end_date = s_obj.end_date;
                    s_obj.daysintervalobjList.add(d_obj);
                } else if (d_obj.end_date.compareTo(s_obj.end_date) <= 0 && d_obj.start_date.compareTo(s_obj.start_date) < 0 && d_obj.end_date.compareTo(s_obj.start_date) > 0) {
                    DaysIntervalObj intervalObj_start = new DaysIntervalObj(); //the d_obj from s_obj.start_date to d_obj.end_date
                    intervalObj_start.start_date = s_obj.start_date;
                    intervalObj_start.end_date = d_obj.end_date;
                    s_obj.daysintervalobjList.add(d_obj);
                }
            }
            System.out.println(s_obj);
        }


    }

    /**
     * Load GPS data and store info in this.visited_result and this.result
     */
    private void loadGPSData() {
        System.out.println("=======================================================================================");
        StringBuffer sb = new StringBuffer();
        BufferedReader br = null;
        int linenumber = 0;
        PData pd = new PData();

        int total_line_number = 0;
        try {
            br = new BufferedReader(new FileReader(this.GPSData));
            String line = null;
            while ((line = br.readLine()) != null) {
                total_line_number++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int pre_progress = 0;

        try (ProgressBar pbar = new ProgressBar("Read the GPS File", 100, ProgressBarStyle.ASCII)) {
            pbar.setExtraMessage("Reading.....");
            pbar.stepBy(0);


            br = new BufferedReader(new FileReader(this.GPSData));


            String line = null;
            while ((line = br.readLine()) != null) {
                linenumber++;
                //jump the header
                if (linenumber == 1) {
                    continue;
                }

                int current_progress = (int) (((linenumber + 1) * 1.0 / total_line_number) * 100);
                if (current_progress != pre_progress) {
                    pbar.step();
                    pre_progress = current_progress;
                }


//                System.out.println(line);
//                System.out.println(line.split(",")[0]);
//                System.out.println(line.split(",")[1]);
//                System.out.println(line.split(",")[2]);
//                System.out.println(line.split(",")[3]);
//                System.out.println(line.split(",")[4]);
//                System.out.println(line.split(",")[5]);
//                System.out.println(line.split(",").length);

                String[] infos = line.split(",");
//                System.out.println(infos.length);

                String gpsId = infos[0];
                String cowId = infos[1];
                String date = infos[2];
                String time = infos[3];
                double northing = Double.parseDouble(infos[4]);
                double easting = Double.parseDouble(infos[5]);

                long pixelId = getPixelID(northing, easting); //get the pixel that could include the current gps record

                //Store the visited information
                if (pixelId != -1) {
                    if (this.visited_result.containsKey(cowId)) {
                        HashMap<Long, Pair<Integer, Double>> pixelMapping = this.visited_result.get(cowId);
                        if (pixelMapping.containsKey(pixelId)) {
                            int visit_time = pixelMapping.get(pixelId).getKey() + 1;
                            double time_stay = pixelMapping.get(pixelId).getValue();
                            pixelMapping.put(pixelId, new MutablePair<>(visit_time, time_stay));
                            visited_result.put(cowId, pixelMapping);
                        } else {
                            pixelMapping.put(pixelId, new MutablePair<>(1, 0.0));
                            this.visited_result.put(cowId, pixelMapping);
                        }

                    } else {
                        HashMap<Long, Pair<Integer, Double>> pixelMapping = new HashMap<>();
                        pixelMapping.put(pixelId, new MutablePair<>(1, 0.0));
                        this.visited_result.put(cowId, pixelMapping);
                    }
                }

                if (pd.date != null & pd.time != null) {
                    double time_stay = getDifferTime(pd.date, pd.time, date, time);
                    String pre_cow_id = pd.cowId;
                    long pre_pixel_id = pd.pixel_id;


                    if (pre_pixel_id != -1 && pre_cow_id.equals(cowId)) {
                        int visit_time_in_pixel = this.visited_result.get(pre_cow_id).get(pre_pixel_id).getKey();
                        double time_stay_in_pixel = this.visited_result.get(pre_cow_id).get(pre_pixel_id).getValue() + time_stay;
                        this.visited_result.get(pre_cow_id).put(pre_pixel_id, new MutablePair<>(visit_time_in_pixel, time_stay_in_pixel));
                    }
                }

                //System.out.println(visited_result.size());

                /*previous record is empty.
                  1. just read the second line.
                  2. Different cowid
                  3. Different date
                */
                if (cowId.equals(pd.cowId) && date.equals(pd.date)) // if the same cow and the GPS record is in the same day
                {
                    //calculate the speed of the cow start from the previous record
                    double distance = Math.abs(Math.sqrt(Math.pow(pd.easting - easting, 2) + Math.pow(pd.northing - northing, 2)));
                    double time_spend = getDifferTime(pd.time, time, date);
                    double speed = distance / time_spend; //the time interval to calculate the speed

                    //if the speed need further processing
                    if (speed >= this.min_speed && speed <= this.max_speed) {
                        pixelId = getPixelID(northing, easting); //get the pixel that could include the current gps record

                        if (pixelId == -1) //if I can not find such a pixel
                        {
                            pd.setAttrs(infos, pixelId);
                            continue;
                        }

                        if (this.result.containsKey(cowId)) {
                            HashMap<Long, HashSet<String>> pixelMapping = this.result.get(cowId);
                            if (pixelMapping.containsKey(pixelId)) {
                                HashSet<String> datelist = pixelMapping.get(pixelId);
                                if (!datelist.contains(date))
                                    datelist.add(date);
                            } else {
                                HashSet<String> datelist = new HashSet<>();
                                datelist.add(date);
                                pixelMapping.put(pixelId, datelist);
                                this.result.put(cowId, pixelMapping);
                            }

                        } else {
                            HashMap<Long, HashSet<String>> pixelMapping = new HashMap<>();
                            HashSet<String> datelist = new HashSet<>();
                            datelist.add(date);
                            pixelMapping.put(pixelId, datelist);
                            this.result.put(cowId, pixelMapping);
                        }
                    }
                }
                pd.setAttrs(infos, pixelId);
            }
            br.close();
        } catch (Exception e) {
            System.err.println("Can not open the Coordination file, please check it. ");
        }
        System.out.println("read the gps file done" + "   " + linenumber);
    }


    /**
     * Find the pixel id of the given northing and easting coordination
     *
     * @param northing the value of the northing coordination
     * @param easting  the value of the easting coordination
     * @return the pixel id, if can not find the cell of the pixel, return -1.
     */
    private long getPixelID(double northing, double easting) {
        long result = -1;
        for (Map.Entry<Long, Pair<Double, Double>> e : this.pixelList.entrySet()) {
            double x = e.getValue().getKey(); //northing
            double y = e.getValue().getValue(); //easting
            //easting puls, norting sub
            if ((northing < x && northing > x - this.range_size) && (easting > y && easting < y + this.range_size)) { // Need to check it carefully.
                result = e.getKey();
                break;
            }
        }
//        System.out.println(result);
        return result;
    }


}
