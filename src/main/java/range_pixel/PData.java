package range_pixel;

public class PData {

    public String gpsId, cowId, date, time;
    public double northing, easting;
    public long pixel_id=-1;

    public boolean isNull() {
        return isNull;
    }

    public void setNull(boolean aNull) {
        isNull = aNull;
    }

    private boolean isNull;

    public PData() {
        this.isNull = true;
    }

    public void initlizePData() {
        this.gpsId = this.cowId = this.date = this.time = "";
        this.northing = this.easting = 0.0;
        this.pixel_id = -1;
    }

    public boolean isnull() {
        return this.isNull;
    }

    public void setAttrs(String[] infos, long pixel_id) {
        gpsId = infos[0];
        cowId = infos[1];
        date = infos[2];
        time = infos[3];
        northing = Double.parseDouble(infos[4]);
        easting = Double.parseDouble(infos[5]);
        this.pixel_id = pixel_id;
    }
}
