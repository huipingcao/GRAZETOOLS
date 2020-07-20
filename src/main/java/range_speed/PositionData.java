package range_speed;

import java.util.Date;

class PositionData
{
    String cowidPrevious="";
    String cowidCurrent="";
    String Year="";
    String Treatment="";
    String PreviousTime="";
    String CurrentTime="";

    String PreviousDate="";
    String CurrentDate="";

    String PreviousDT = "";
    String CurrentDT = "";
    Date TimePrevious=null;
    Date TimeCurrent=null;
    Date DateCurrent = null;
    Date DatePrevious = null;
    Date DTPrevious = null;
    Date DTCurrent = null;
    long DateDifference=0;
    long DTDifferernce;


    //Positions
    double x1=0.0;//previous northing
    double y1=0.0;//previous easting
    double x2=0.0;//current northing
    double y2=0.0;//current easting

    double Woodland=0.0;//wood
    double DayCount=0.0;
    double PostNightCount=0.0;
    double PreNightCount=0.0;

    int TotalWoodLandCount=0;
    int PreNightWoodLandCount=0;
    int PostNightWoodLandCount=0;
    int DayWoodLandCount=0;

    double TotalNumberofWoodlandcolumn=0.0;
    double TotalWoodLandArea=0.0;
    double DayWoodLandArea=0.0;
    double PreNightWoodLandArea=0.0;
    double PostNightWoodLandArea=0.0;



    //Variable for tracking the positions of cow in pre,post,day times
    double pd2=0.0;
    double pn2=0.0;
    double pd1=0.0;
    double pn1=0.0;
    double dd1=0.0;
    double dn1=0.0;
    double dd2=0.0;
    double dn2=0.0;
    double sd1=0.0;
    double sn1=0.0;

    double p1=0.0;
    double n1=0.0;
    double p2=0.0;
    double n2=0.0;
    //variable to keep track of distance
    double dist=0.0;
    double day_dist=0.0;
    double pre_dist=0.0;
    double post_dist=0.0;
    //variable to keep track of siniosities

    double pre_sin=0.0;
    double post_sin=0.0;
    double day_sin=0.0;
    double tot_sin=0.0;
    //Global Flags used
    int flag2=99;
    double day=0.0;
    double post=0.0;

    public long DayTime;   //Store how many second in the day.
    public long Pre_time;  //Store how many second before sun rise.
    public long Day_time;  //Store how many second after sun rise and before sun set.
    public long Post_time; //Store how many second after sun set

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("==========================================").append("\n");
        sb.append("cowidPrevious:").append(cowidPrevious).append("\n");
        sb.append("cowidCurrent:").append(cowidCurrent).append("\n");
        sb.append("PreviousDate:").append(PreviousDate).append("\n");
        sb.append("CurrentDate:").append(CurrentDate).append("\n");
        sb.append("Year:").append(Year).append("\n");
        sb.append("Treatment:").append(Treatment).append("\n");
        sb.append("PreviousTime:").append(PreviousTime).append("\n");
        sb.append("CurrentTime:").append(CurrentTime).append("\n");
        sb.append("TimePrevious:").append(TimePrevious).append("\n");
        sb.append("TimeCurrent:").append(TimeCurrent).append("\n");
        sb.append("DateCurrent:").append(DateCurrent).append("\n");
        sb.append("DatePrevious:").append(DatePrevious).append("\n");
        sb.append("DateDifference:").append(DateDifference).append("\n");
        sb.append("x1:").append(x1).append("\n");
        sb.append("y1:").append(y1).append("\n");
        sb.append("x2:").append(x2).append("\n");
        sb.append("y2:").append(y2).append("\n");
        sb.append("Woodland:").append(Woodland).append("\n");
        sb.append("DayCount:").append(DayCount).append("\n");
        sb.append("PostNightCount:").append(PostNightCount).append("\n");
        sb.append("PreNightCount:").append(PreNightCount).append("\n");
        sb.append("TotalWoodLandCount:").append(TotalWoodLandCount).append("\n");
        sb.append("PreNightWoodLandCount:").append(PreNightWoodLandCount).append("\n");
        sb.append("PostNightWoodLandCount:").append(PostNightWoodLandCount).append("\n");
        sb.append("DayWoodLandCount:").append(DayWoodLandCount).append("\n");
        sb.append("TotalNumberofWoodlandcolumn:").append(TotalNumberofWoodlandcolumn).append("\n");
        sb.append("TotalWoodLandArea:").append(TotalWoodLandArea).append("\n");
        sb.append("DayWoodLandArea:").append(DayWoodLandArea).append("\n");
        sb.append("PreNightWoodLandArea:").append(PreNightWoodLandArea).append("\n");
        sb.append("PostNightWoodLandArea:").append(PostNightWoodLandArea).append("\n");
        sb.append("pd2:").append(pd2).append("\n");
        sb.append("pn2:").append(pn2).append("\n");
        sb.append("pd1:").append(pd1).append("\n");
        sb.append("pn1:").append(pn1).append("\n");
        sb.append("dd1:").append(dd1).append("\n");
        sb.append("dn1:").append(dn1).append("\n");
        sb.append("dd2:").append(dd2).append("\n");
        sb.append("dn2:").append(dn2).append("\n");
        sb.append("sd1:").append(sd1).append("\n");
        sb.append("sn1:").append(sn1).append("\n");
        sb.append("p1:").append(p1).append("\n");
        sb.append("p2:").append(p2).append("\n");
        sb.append("n1:").append(n1).append("\n");
        sb.append("n2:").append(n2).append("\n");
        sb.append("dist:").append(dist).append("\n");
        sb.append("day_dist:").append(day_dist).append("\n");
        sb.append("pre_dist:").append(pre_dist).append("\n");
        sb.append("post_dist:").append(post_dist).append("\n");
        sb.append("pre_sin:").append(pre_sin).append("\n");
        sb.append("post_sin:").append(post_sin).append("\n");
        sb.append("day_sin:").append(day_sin).append("\n");
        sb.append("tot_sin:").append(tot_sin).append("\n");
        sb.append("flag2:").append(flag2).append("\n");
        sb.append("day:").append(day).append("\n");
        sb.append("post:").append(post).append("\n");
        sb.append("PreviousDT:").append(PreviousDT).append("\n");
        sb.append("CurrentDT:").append(CurrentDT).append("\n");
        sb.append("DTPrevious:").append(DTPrevious).append("\n");
        sb.append("DTCurrent:").append(DTCurrent).append("\n");
        sb.append("DTDifferernce:").append(DTDifferernce).append("\n");
        sb.append("DayTime:").append(DayTime).append("\n");
        sb.append("Pre_time:").append(Pre_time).append("\n");
        sb.append("Day_time:").append(Day_time).append("\n");
        sb.append("Post_time:").append(Post_time).append("\n");
//        sb.append("==========================================").append("\n");
        return sb.toString();
    }
}