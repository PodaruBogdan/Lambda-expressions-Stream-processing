package activityProcess;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MonitoredData {
    private Date start_time;
    private Date end_time;
    private String activity;

    public MonitoredData(String[] data) {
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            this.start_time = format.parse(data[0]);
            this.end_time = format.parse(data[1]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.activity = data[2];
    }

    public String toString() {
        return start_time+" "+end_time+" "+activity;
    }
    public String getStartIdentifier()
    {
        return ""+start_time.getYear()+"-"+start_time.getMonth()+"-"+start_time.getDate();
    }
    public String getEndIdentifier()
    {
        return ""+end_time.getYear()+"-"+end_time.getMonth()+"-"+end_time.getDate();
    }
    public Date getStart_time() {
        return start_time;
    }

    public Date getEnd_time() {
        return end_time;
    }

    public String getActivity() {
        return activity;
    }
}
