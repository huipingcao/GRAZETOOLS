package range_speed;

public class PData {

    public String gpsId, cowId, date, time;
    public double northing, easting ;
    public int woodland;
//    private boolean isNull;

    public PData() {
    }

//    public boolean isNull() {
//        return isNull;
//    }
//
//    public void setNull(boolean aNull) {
//        isNull = aNull;
//    }

    public void initlizePData() {
        this.gpsId = this.cowId = this.date = this.time = "";
        this.northing = this.easting = 0.0;
    }

//    public boolean isnull() {
//        return this.isNull;
//    }

    public void setAttrs(String[] infos) {
        cowId = infos[0];
        date = infos[1];
        time = infos[2];
        northing = Double.parseDouble(infos[3]);
        easting = Double.parseDouble(infos[4]);
        woodland = Integer.parseInt(infos[5]);
    }
}
