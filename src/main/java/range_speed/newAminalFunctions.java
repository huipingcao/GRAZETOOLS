package range_speed;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Input: GPS and Time data files
 * Output: MCP_Results.csv(Calculates convex hull vertices of cow movements on each day, pre-day, day-time and post-day)  
 * MCP_Area_Results.csv(Calculates convex hull area of cow movements on each day, pre-day, day-time and post-day)
 * movement_partition.csv(calculates cows movement partition)
 * time_slots.csv (the time slots of the activities for each cow in each day
 */
public class newAminalFunctions {

    String dataFile = "";
    String TimeFile = "";
    double rest_speed = 5;  //upper bound of the speed (m/min) of the rest movement
    double grazing_speed = 15;  //upper bound of the speed (m/min) of the grazing movement 
//    int traveling_speed = 50;

    HashMap<String, HashMap<String, ArrayList<Double[]>>> pointsMap = new HashMap<>(); //cowid -> <date, List of points>
    HashMap<String, HashSet<Pair<String, double[]>>> points_time_Map = new HashMap<>(); //cowid -> <date(day+time), List of points>

    HashMap<String, HashMap<String, HashSet<Pair<Double, Double>>>> pre_pointsMap = new HashMap<>(); //cowid -> <date, List of points>
    HashMap<String, HashMap<String, HashSet<Pair<Double, Double>>>> day_pointsMap = new HashMap<>(); //cowid -> <date, List of points>
    HashMap<String, HashMap<String, HashSet<Pair<Double, Double>>>> post_pointsMap = new HashMap<>(); //cowid -> <date, List of points>


    HashSet<String> dateList = new HashSet<>();
    HashSet<String> cowList = new HashSet<>();
    DateFormat TimeFormatter = new SimpleDateFormat("hh:mm:ss a");
    DateFormat DateTimeFormatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");


    HashMap<String, Pair<String, String>> timeObj = new HashMap<>(); //data -> <Sunrise and Sunset>
    ConvexHull ch = new ConvexHull();
    private double threshold = 4; //determine whether one GPS record is active or not 
    private int lag = 5; //number of GPS records need to be considered in previous and following


    public newAminalFunctions(String fileDataPosition, String fileTime, double rest_speed, double grazing_speed, double threshold, int lag) {
        this.dataFile = fileDataPosition;
        this.TimeFile = fileTime;
        this.rest_speed = rest_speed;
        this.grazing_speed = grazing_speed;
        this.threshold = threshold;
        this.lag = lag;

//        System.out.println(this.dataFile);
//        System.out.println(this.TimeFile);
        readTime();
        readGPSData();


    }

    /**
     * Read time data and store info in this.timeObj(data -> <Sunrise and Sunset>)
     */
    private void readTime() {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = null;
        int linenumber = 0;
        try {
            br = new BufferedReader(new FileReader(this.TimeFile));
            String line = null;
            while ((line = br.readLine()) != null) {
                linenumber++;
                //jump the header
                if (linenumber == 1) {
                    continue;
                }

                String infos[] = line.split(",");
                String date = infos[0];
                String sunrise_time = infos[1];
                String sunset_time = infos[2];
                this.timeObj.put(date, new MutablePair<>(sunrise_time, sunset_time));

            }
            br.close();
        } catch (Exception e) {
            System.err.println("Can not open the time file, please check it. ");
        }
        System.out.println("Read the time file done" + "   " + linenumber);
//        System.out.println("--------------------------------------------------");
    }

    /**
     * Read GPS data and store info in this.cowList and this.dateList
     * It uses info of timeObj and divides data into three time periods(stores in pre_pointsMap, day_pointsMap and post_pointsMap)
     */
    private void readGPSData() {
        BufferedReader br = null;
        int linenumber = 0;

        try {
            br = new BufferedReader(new FileReader(this.dataFile));
            String line = null;
            while ((line = br.readLine()) != null) {
                linenumber++;
                //jump the header
                if (linenumber == 1) {
                    continue;
                }

                String[] infos = line.split(",");
                if (infos.length >= 5) {
                    String c_cowid = infos[0];
                    String c_date = infos[1];
                    String c_time = infos[2].trim();
                    double c_northing = Double.valueOf(infos[3]);
                    double c_easting = Double.valueOf(infos[4]);

                    this.cowList.add(c_cowid);
                    this.dateList.add(c_date);

//                    System.out.println(linenumber + "," + c_cowid + " , " + c_date + " , " + c_time + " , " + c_northing + " , " + c_easting);


                    //store data to the structure which is used to calculate the proportions of different type of movement
                    if (this.points_time_Map.containsKey(c_cowid)) {
                        HashSet<Pair<String, double[]>> d = points_time_Map.get(c_cowid);
                        d.add(new MutablePair<>(c_date + " " + c_time, new double[]{c_northing, c_easting}));
                    } else {
                        HashSet<Pair<String, double[]>> d = new HashSet<>();
                        d.add(new MutablePair<>(c_date + " " + c_time, new double[]{c_northing, c_easting}));
                        this.points_time_Map.put(c_cowid, d);
                    }

//                    System.out.println(c_date);
                    String sun_rise = timeObj.get(c_date).getKey();
                    String sun_set = timeObj.get(c_date).getValue();
                    int ptype = getPartitionType(c_time, sun_rise, sun_set);
//                    System.out.println(sun_rise+"  "+sun_set+" "+getPartitionType(c_time,sun_rise,sun_set));

                    if (ptype == 0) {
//                        System.out.println(linenumber+","+c_cowid + " , " + c_date + " , " + c_time + " , " + c_northing + " , " + c_easting);
//                        System.out.println(sun_rise+"  "+sun_set+" "+getPartitionType(c_time,sun_rise,sun_set));
                        if (this.pre_pointsMap.containsKey(c_cowid)) {
                            HashMap<String, HashSet<Pair<Double, Double>>> d = pre_pointsMap.get(c_cowid);
                            if (d.containsKey(c_date)) {
                                HashSet<Pair<Double, Double>> pList = d.get(c_date);
                                pList.add(new MutablePair<>(c_easting, c_northing));
                            } else {
                                HashSet<Pair<Double, Double>> pList = new HashSet<>();
                                pList.add(new MutablePair<>(c_easting, c_northing));
                                d.put(c_date, pList);
                            }
                        } else {
                            HashMap<String, HashSet<Pair<Double, Double>>> d = new HashMap<>();
                            HashSet<Pair<Double, Double>> pList = new HashSet<>();
                            pList.add(new MutablePair<>(c_easting, c_northing));
                            d.put(c_date, pList);
                            this.pre_pointsMap.put(c_cowid, d);
                        }
                    } else if (ptype == 1) {
                        if (this.day_pointsMap.containsKey(c_cowid)) {
                            HashMap<String, HashSet<Pair<Double, Double>>> d = day_pointsMap.get(c_cowid);
                            if (d.containsKey(c_date)) {
                                HashSet<Pair<Double, Double>> pList = d.get(c_date);
                                pList.add(new MutablePair<>(c_easting, c_northing));
                            } else {
                                HashSet<Pair<Double, Double>> pList = new HashSet<>();
                                pList.add(new MutablePair<>(c_easting, c_northing));
                                d.put(c_date, pList);
                            }
                        } else {
                            HashMap<String, HashSet<Pair<Double, Double>>> d = new HashMap<>();
                            HashSet<Pair<Double, Double>> pList = new HashSet<>();
                            pList.add(new MutablePair<>(c_easting, c_northing));
                            d.put(c_date, pList);
                            this.day_pointsMap.put(c_cowid, d);
                        }

                    } else if (ptype == 2) {
                        if (this.post_pointsMap.containsKey(c_cowid)) {
                            HashMap<String, HashSet<Pair<Double, Double>>> d = post_pointsMap.get(c_cowid);
                            if (d.containsKey(c_date)) {
                                HashSet<Pair<Double, Double>> pList = d.get(c_date);
                                pList.add(new MutablePair<>(c_easting, c_northing));
                            } else {
                                HashSet<Pair<Double, Double>> pList = new HashSet<>();
                                pList.add(new MutablePair<>(c_easting, c_northing));
                                d.put(c_date, pList);
                            }
                        } else {
                            HashMap<String, HashSet<Pair<Double, Double>>> d = new HashMap<>();
                            HashSet<Pair<Double, Double>> pList = new HashSet<>();
                            pList.add(new MutablePair<>(c_easting, c_northing));
                            d.put(c_date, pList);
                            this.post_pointsMap.put(c_cowid, d);
                        }

                    }

                } else {
                    System.out.println(linenumber + ":" + line);
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("read the gps file done" + "   " + linenumber);
    }


    /**
     * Calculates convex hull vertices of cow movements on each day, pre-day, day-time and post-day and write data into MCP_Results.csv, which are sorted by Cow_id, then by day time partition, Date
     * Calculates convex hull area of cow movements on each day, pre-day, day-time and post-day and write info into MCP_Area_Results.csv, which are sorted by Cow_id, then by Date
     * Algorithm: Andrew's monotone chain convex hull algorithm(The function accept the GPS locations of one cow, the vertices of the convex hull are returned. The complexity for calculation on two-dimensional space is O(nlog(n)))
     */
    public void convexHull() {

        BufferedWriter area_bw = null;
        FileWriter area_fw = null;
        BufferedWriter bw = null;
        FileWriter fw = null;

        try {


            File file = new File("MCP_Results.csv");
            File area_file = new File("MCP_Area_Results.csv");
            if (file.exists()) {
                file.delete();
            }

            if (area_file.exists()) {
                area_file.delete();
            }

            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);


            area_fw = new FileWriter(area_file.getAbsoluteFile(), true);
            area_bw = new BufferedWriter(area_fw);

            area_bw.write("Cow_id,Date,Pre_MCP_area,Day_MCP_Area,Post_MCP_Area,All_Day_area\n");
            bw.write("Cow_id,Date,Daily_period,Easting,Northing\n");


            List<String> sorted_cow_List = new ArrayList(this.cowList);
            Collections.sort(sorted_cow_List, new comparatorStrCowID());
            List<String> sorted_date_List = new ArrayList(this.dateList);
            Collections.sort(sorted_date_List, new comparatorDate());

            for (String cowid : sorted_cow_List) {
                for (String date : sorted_date_List) {
//                    System.out.println(cowid+" "+date+" "+(this.pre_pointsMap.get(cowid)==null)+" "+(this.day_pointsMap.get(cowid)==null)+" "+(this.post_pointsMap.get(cowid)==null));
                    HashSet<Pair<Double, Double>> pre_points=new HashSet<>();
                    HashSet<Pair<Double, Double>> day_points=new HashSet<>();
                    HashSet<Pair<Double, Double>> post_points=new HashSet<>();

                    if(this.pre_pointsMap.get(cowid)!=null){
                        pre_points = this.pre_pointsMap.get(cowid).get(date);
                    }
                    if(this.day_pointsMap.get(cowid)!=null){
                        day_points = this.day_pointsMap.get(cowid).get(date);
                    }
                    if(this.post_pointsMap.get(cowid)!=null){
                        post_points = this.post_pointsMap.get(cowid).get(date);
                    }

                    HashSet<Pair<Double, Double>> allDay_points = new HashSet<>();

                    if (pre_points == null && day_points == null && post_points == null) {
                        continue;
                    }

//                System.out.println(date);
                    StringBuffer sb = new StringBuffer();
                    StringBuffer a_sb = new StringBuffer();

                    a_sb.append(cowid).append(",").append(date).append(",");

                    if (pre_points != null) {
                        allDay_points.addAll(pre_points);
                        Point[] convex_points = FindConvexHull(pre_points);
                        for (int i = 0; i < convex_points.length; i++) {
                            if (convex_points[i] != null) {
                                sb.append(cowid).append(",").append(date).append(",").append("Pre_MCP").append(",");
                                sb.append(convex_points[i].x).append(",");
                                sb.append(convex_points[i].y).append("\n");
                            }
                        }
                        a_sb.append(convex_area(convex_points)).append(",");
                    } else {
                        a_sb.append(",");
                    }

                    if (day_points != null) {
                        allDay_points.addAll(day_points);
                        Point[] convex_points = FindConvexHull(day_points);
                        for (int i = 0; i < convex_points.length; i++) {
                            if (convex_points[i] != null) {
                                sb.append(cowid).append(",").append(date).append(",").append("Day_MCP").append(",");
                                sb.append(convex_points[i].x).append(",");
                                sb.append(convex_points[i].y).append("\n");
                            }
                        }
                        a_sb.append(convex_area(convex_points)).append(",");
                    } else {
                        a_sb.append(",");
                    }

                    if (post_points != null) {
                        allDay_points.addAll(post_points);
                        Point[] convex_points = FindConvexHull(post_points);
                        for (int i = 0; i < convex_points.length; i++) {
                            if (convex_points[i] != null) {
                                sb.append(cowid).append(",").append(date).append(",").append("Post_MCP").append(",");
                                sb.append(convex_points[i].x).append(",");
                                sb.append(convex_points[i].y).append("\n");
                            }
                        }
                        a_sb.append(convex_area(convex_points)).append(",");

                    } else {
                        a_sb.append(",");
                    }

                    Point[] convex_points = FindConvexHull(allDay_points);
                    for (int i = 0; i < convex_points.length; i++) {
                        if (convex_points[i] != null) {
                            sb.append(cowid).append(",").append(date).append(",").append("All_Day_MCP").append(",");
                            sb.append(convex_points[i].x).append(",");
                            sb.append(convex_points[i].y).append("\n");
                        }
                    }
                    a_sb.append(convex_area(convex_points)).append("\n");

                    bw.write(sb.toString());
                    area_bw.write(a_sb.toString());
//                }
                }
//            break;
            }
        } catch (IOException e) {
            e.printStackTrace();

        } finally {

            try {

                if (bw != null)
                    bw.close();

                if (fw != null)
                    fw.close();

                if (area_bw != null)
                    area_bw.close();

                if (area_fw != null)
                    area_fw.close();

                System.out.println("Done!! See MCP_Results.csv for MCP of each cow for each day.");
                System.out.println("Done!! See MCP_Area_Results.csv for MCP of each cow for each day.");

            } catch (IOException ex) {

                ex.printStackTrace();

            }
        }
    }

    /**
     * @param cur_date the string of the current time
     * @param Sunrise  the sunrise time of the date of the input time records
     * @param Sunset   the sunset time of the date of the input time records
     * @return the type of the current time, 0 is the pre day, 1 is the day time, 2 is the post day
     */
    //Todo: consider the equal situations, for example, one date&time is equals to the sunrise or sunset time of the day
    public int getPartitionType(String cur_date, String Sunrise, String Sunset) {
        Date Sunrise_D = null;
        Date Sunset_D = null;
        try {
            Sunrise_D = this.TimeFormatter.parse(Sunrise);
            Sunset_D = this.TimeFormatter.parse(Sunset);

            Date other = this.TimeFormatter.parse(cur_date);

            if (other.before(Sunrise_D)) {
                return 0;
            } else if (other.after(Sunset_D)) {
                return 2;
            } else {
                return 1;
            }
        } catch (ParseException e) {
            System.out.println(cur_date + "|" + Sunrise + "|" + Sunset);
            System.out.println("There is something wrong with your time formation, please check it 3");
            System.exit(0);
        }

        return -1;
    }

    /**
     * Find convex hull points by given GPS data
     * Algorithm: It calls ConvexHull.java, which uses Andrew's monotone chain convex hull algorithm
     * @param points
     * @return 
     */
    public Point[] FindConvexHull(HashSet<Pair<Double, Double>> points) {
        Point[] p = new Point[points.size()];
        int i = 0;
        for (Pair<Double, Double> dd : points) {
//            System.out.println(dd.getKey() + "," + dd.getValue());
            p[i] = new Point();
            p[i].x = dd.getKey(); // Read X coordinate
            p[i].y = dd.getValue(); // Read y coordinate
            i++;
        }


        Point[] hull = ch.convex_hull(p).clone();
//        double[] xlist = new double[hull.length];
//        double[] ylist = new double[hull.length];
//        for (i = 0; i < hull.length; i++) {
//            if (hull[i] != null) {
//                xlist[i] = hull[i].x;
//                ylist[i] = hull[i].y;
//            }
//        }

        return hull;
    }

    /**
     * Calculates convex hull area of cow movements by given convex hull points
     * Algorithm: Polygon Area
     * @param pin_s 
     * @return
     */
    public double convex_area(Point[] pin_s) {
        double area = 0;
        for (int i = 0; i < pin_s.length; i++) {
            int i1 = i + 1;
            int i2 = i - 1;

            if (i1 == pin_s.length) {
                i1 = 0;
            }

            if (i2 == -1) {
                i2 = pin_s.length - 1;
            }
            area += pin_s[i].x * (pin_s[i1].y - pin_s[i2].y);
        }
        area /= 2.0;
        return area;
    }

    /**
     * Call movementParitionForCow() to calculates cows movement partition, represented as percentage, according to the user inputted upper bound of the rest movement speed and the grazing movement speed. And all data are partitioned into three time periods (pre-day, day-time and post-day). And write data into movement_partition.csv, sorted by cowID
     */
    public void movementPartition() {
        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            File file = new File("movement_partition.csv");
            if (file.exists()) {
                file.delete();
            }

            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write("Cow_id,Date,r_pre,g_pre,t_pre,r_day,g_day,t_day,r_post,g_post,t_post,r_total,g_total,t_total\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }


        TreeMap<String, HashSet<Pair<String, double[]>>> s_by_cowId = new TreeMap<>(new comparatorStrCowID());
        s_by_cowId.putAll(this.points_time_Map);

        for (String cow_id : s_by_cowId.keySet()) {
            movementParitionForCow(cow_id);
        }

        System.out.println("Done!! See movement_partition.csv for the movement for each cow for each day");

    }


    /**
     * Get movement states by given speed
     * @param speed
     * @return 0-rest 1-grazing 2-running
     */
    public int movement_type(double speed) {
        if (speed <= this.rest_speed) {
            return 0;
        } else if (speed <= this.grazing_speed) {
            return 1;
        } else {
            return 2;
        }
    }

    /**
     * Counts the percentage of each cow that was resting, walking and running during the whole day, the pre-day, the daytime, and the post-day according given time date.
     * @param cowid
     */
    public void movementParitionForCow(String cowid) {

        BufferedWriter area_bw = null;
        FileWriter area_fw = null;
        BufferedWriter bw = null;
        FileWriter fw = null;


        HashSet<Pair<String, double[]>> dtp_list_of_cow = this.points_time_Map.get(cowid); //date --->> list of points

        List<String> sorted_date_List = new ArrayList(this.dateList);
        Collections.sort(sorted_date_List, new comparatorDate());

        //Sorted the records by date and time, and put them into one ArrayList
        TreeSet<Pair<String, double[]>> ordered_time_list = new TreeSet<>(new SortByTime());
        ordered_time_list.addAll(dtp_list_of_cow);
        ArrayList<Pair<String, double[]>> t_list = new ArrayList<>(ordered_time_list);


        HashMap<String, int[][]> result = new HashMap<>(); //date -->>  row(time_type) column(movement type)

        for (int i = 1; i < t_list.size(); i++) {
            String c_date = t_list.get(i).getKey().split(" ")[0];
            String c_time = t_list.get(i).getKey().split(" ")[1] + " " + t_list.get(i).getKey().split(" ")[2];

            double c_north = t_list.get(i).getValue()[0];
            double c_east = t_list.get(i).getValue()[1];

            double p_north = t_list.get(i - 1).getValue()[0];
            double p_east = t_list.get(i - 1).getValue()[1];

            try {
                Date c_d = this.DateTimeFormatter.parse(t_list.get(i).getKey());
                Date p_d = this.DateTimeFormatter.parse(t_list.get(i - 1).getKey());

                long differ_mins = (c_d.getTime() - p_d.getTime()) / 1000 / 60;

                double c_speed = Math.sqrt(Math.pow(c_north - p_north, 2) + Math.pow(c_east - p_east, 2)) / differ_mins;
                int m_type = movement_type(c_speed);//get movement type

                String sun_rise = timeObj.get(c_date).getKey();
                String sun_set = timeObj.get(c_date).getValue();
                int t_type = getPartitionType(c_time, sun_rise, sun_set); //get
//                System.out.println(t_list.get(i).getKey() + " " + differ_mins + " " + c_speed+" "+m_type+" ");

                if (result.containsKey(c_date)) {
                    int[][] p_array = result.get(c_date);
                    p_array[m_type][t_type] += 1;
                } else {
                    int[][] p_array = new int[3][3];
                    p_array[m_type][t_type] = 1;
                    result.put(c_date, p_array);
                }


            } catch (ParseException e) {
                e.printStackTrace();
            }
        }


        try {
            File file = new File("movement_partition.csv");
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            TreeMap<String, int[][]> sorted_result = new TreeMap<>(new SortByDate());
            sorted_result.putAll(result);
            for (Map.Entry<String, int[][]> e : sorted_result.entrySet()) {
                int[][] p_array = e.getValue();

                int pre_t = p_array[0][0] + p_array[1][0] + p_array[2][0];
                int day_t = p_array[0][1] + p_array[1][1] + p_array[2][1];
                int post_t = p_array[0][2] + p_array[1][2] + p_array[2][2];
                int total_t = pre_t + day_t + post_t;

                double r_pre_p = p_array[0][0] * 1.0 / pre_t; //rest_pre_percentage
                double g_pre_p = p_array[1][0] * 1.0 / pre_t; //graze_pre_percentage
                double t_pre_p = p_array[2][0] * 1.0 / pre_t; //travel_pre_percentage


                double r_day_p = p_array[0][1] * 1.0 / day_t; //rest_day_percentage
                double g_day_p = p_array[1][1] * 1.0 / day_t; //graze_day_percentage
                double t_day_p = p_array[2][1] * 1.0 / day_t; //travel_day_percentage

                double r_post_p = p_array[0][2] * 1.0 / post_t; //rest_post_percentage
                double g_post_p = p_array[1][2] * 1.0 / post_t; //graze_post_percentage
                double t_post_p = p_array[2][2] * 1.0 / post_t; //travel_post_percentage


                double r_total_p = (p_array[0][0] + p_array[0][1] + p_array[0][2]) * 1.0 / total_t; //rest_total_percentage
                double g_total_p = (p_array[1][0] + p_array[1][1] + p_array[1][2]) * 1.0 / total_t; //graze_total_percentage
                double t_total_p = (p_array[2][0] + p_array[2][1] + p_array[2][2]) * 1.0 / total_t; //travel_total_percentage

                bw.write(cowid + "," + e.getKey() + ",");
                bw.write(r_pre_p + "," + g_pre_p + "," + t_pre_p + ",");
                bw.write(r_day_p + "," + g_day_p + "," + t_day_p + ",");
                bw.write(r_post_p + "," + g_post_p + "," + t_post_p + ",");
                bw.write(r_total_p + "," + g_total_p + "," + t_total_p + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Identifies the activity time slots of the each cow in each day and write data into time_slots.csv
     */
    public void time_slot() {

        //delete before write the file
        File file = new File("time_slots.csv");
        if (file.exists()) {
            file.delete();
        }

        //write the title
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write("CowID,date,activity_time_slots_list\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }


        TreeMap<String, HashSet<Pair<String, double[]>>> s_by_cowId = new TreeMap<>(new comparatorStrCowID());
        s_by_cowId.putAll(this.points_time_Map);

        for (String e : s_by_cowId.keySet()) {
            timeSeriesForCow(e);
        }

        System.out.println("Done!! See time_slots.csv for finding the activities of each cow for each day.");

    }


    /**
     * Find time series of cows movement, which uses treeSet structure to store the ordered time list
     * @param cow_id
     */
    public void timeSeriesForCow(String cow_id) {

        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            File file = new File("time_slots.csv");
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);

            HashSet<Pair<String, double[]>> time_position_set = this.points_time_Map.get(cow_id);

//        List<String> sorted_date_List = new ArrayList(this.dateList);
//        Collections.sort(sorted_date_List, new comparatorDate());

            TreeSet<Pair<String, double[]>> ordered_time_list = new TreeSet<>(new SortByTime());
            ordered_time_list.addAll(time_position_set);
            ArrayList<Pair<String, double[]>> t_list = new ArrayList<>(ordered_time_list);


            ArrayList<String> dateList = new ArrayList<>();

            for (Pair<String, double[]> p : time_position_set) {
                String d = p.getKey().split(" ")[0];
                if (!dateList.contains(d)) {
                    dateList.add(d);
                }
            }

            Collections.sort(dateList, new comparatorDate());
//            System.out.println(dateList.size());

//        String[] d_list = dateList.toArray(new String[dateList.size()]);
//        Collections.sort(d_list, new comparatorDate());

            for (String s : dateList) {
//                System.out.println("=====================================");
//                System.out.println(s);
                ArrayList<Pair<String, Double>> speedList = new ArrayList<>(); //time --> speed
                for (int i = 1; i < t_list.size(); i++) {

                    String c_date = t_list.get(i).getKey().split(" ")[0];
                    String c_time = t_list.get(i).getKey().split(" ")[1] + " " + t_list.get(i).getKey().split(" ")[2];
                    if (c_date.equals(s)) {
                        double c_north = t_list.get(i).getValue()[0];
                        double c_east = t_list.get(i).getValue()[1];

                        double p_north = t_list.get(i - 1).getValue()[0];
                        double p_east = t_list.get(i - 1).getValue()[1];

                        try {
                            Date c_d = this.DateTimeFormatter.parse(t_list.get(i).getKey());
                            Date p_d = this.DateTimeFormatter.parse(t_list.get(i - 1).getKey());

                            long differ_mins = (c_d.getTime() - p_d.getTime()) / 1000 / 60;

                            double c_speed = Math.sqrt(Math.pow(c_north - p_north, 2) + Math.pow(c_east - p_east, 2)) / differ_mins;

//                System.out.println(c_date+" "+c_time+" "+c_speed+" ["+c_north+" "+c_east+"] ["+p_north+" "+p_east+"]");

//                            System.out.print(c_speed + ",");
                            speedList.add(new MutablePair<>(c_time, c_speed));


                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
//                System.out.println();
                double rf_signal[] = findTimeSlot(speedList, this.threshold, this.lag);

                boolean started = false;

//                printArray(rf_signal);
                bw.write(cow_id + "," + s + ",");

                String start_time = "error";
                String end_time = "error";
                for (int i = 0; i < rf_signal.length; i++) {
                    if (rf_signal[i] == 1.0 && !started) {
                        start_time = speedList.get(i).getKey();
                        started = true;
                    } else if (rf_signal[i] == 0 && started) {
                        end_time = speedList.get(i - 1).getKey();
                        if (start_time.equals(end_time)) {
                            bw.write("[" + start_time + "]");
                        } else {
                            bw.write("[" + start_time + "~" + end_time + "]");

                        }
                        started = false;
                    }
                }

                if (started) {
                    end_time = speedList.get(speedList.size() - 1).getKey();
                    if (start_time.equals(end_time)) {
                        bw.write("[" + start_time + "]");
                    } else {
                        bw.write("[" + start_time + "~" + end_time + "]");

                    }
                    started = false;

                }

                bw.write("\n");

            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void printArray(double[] a) {
        for (double d : a) {
            System.out.print(d + ",");
        }

        System.out.println();

    }

    /**
     * Find cows movement time slots by given speedList, threshold and lag
     * @param speedList - time --> speed
     * @param threshold - to determine whether one GPS record is active or not
     * @param lag - number of GPS records need to be considered in previous and following
     * @return a double array of time slots  
     */
    private double[] findTimeSlot(ArrayList<Pair<String, Double>> speedList, double threshold, int lag) {

        int n = speedList.size();
        double[] y = new double[n];
        String[] title = new String[n];
        double[] signals = new double[n];

        for (int i = 0; i < n; i++) {
            y[i] = speedList.get(i).getValue();
            title[i] = speedList.get(i).getKey();
        }

        for (int i = 0; i < n; i++) {
            if (y[i] >= threshold) {
                signals[i] = 1;
            }
        }

        double[] refinded_s_1 = signals;
        double[] refinded_s_2 = refine_signal(refinded_s_1, lag);

        while (!isEquals(refinded_s_1, refinded_s_2)) {
            refinded_s_1 = refinded_s_2;
            refinded_s_2 = refine_signal(refinded_s_1, lag);
        }

//        printArray(refinded_s_2);

        return refinded_s_2;

    }

    /**
     * Determine whether two time slots are equal or not
     * @param refinded_s_1
     * @param refinded_s_2
     * @return boolean value
     */
    private boolean isEquals(double[] refinded_s_1, double[] refinded_s_2) {

        boolean flag = true;

        for (int i = 0; i < refinded_s_1.length; i++) {
            if (refinded_s_1[i] != refinded_s_2[i]) {
                flag = false;
                break;
            }
        }

        return flag;
    }

    /**
     * Get refined signal array by given lag
     * @param signals
     * @param lag
     * @return
     */
    private double[] refine_signal(double[] signals, int lag) {
        int n = signals.length;
        double[] refined_signals = new double[n];
        System.arraycopy(signals, 0, refined_signals, 0, n);

        for (int i = 0; i < n; i++) {
            if (signals[i] == 1 && (noActivityInPrevious(i, signals, lag) && noActivityInFellow(i, signals, lag))) {
                refined_signals[i] = 0;
            } else if (signals[i] == 0 && (!noActivityInPrevious(i, signals, lag) && !noActivityInFellow(i, signals, lag))) {
                refined_signals[i] = 1;
            }
        }

        return refined_signals;
    }

    /**
     * Determine whether the cow has activity in previous by given signal and lag
     * @param index
     * @param signals
     * @param lag
     * @return boolean value
     */
    private boolean noActivityInPrevious(int index, double[] signals, int lag) {
        boolean flag = true;
        int l = 0;

        if (index - lag >= 0) {
            l = index - lag;
        }

        for (int i = l; i < index; i++) {
            if (signals[i] == 1) {
                flag = false;
                break;
            }
        }

        return flag;
    }

    /**
     * Determine whether the cow has activity in fellow by given signal and lag
     * @param index
     * @param signals
     * @param lag
     * @return boolean value
     */
    private boolean noActivityInFellow(int index, double[] signals, int lag) {
        boolean flag = true;
        int u = signals.length;

        if (index + lag < signals.length) {
            u = index + lag + 1;
        }

        for (int i = index + 1; i < u; i++) {
            if (signals[i] == 1) {
                flag = false;
                break;
            }
        }

        return flag;
    }
}