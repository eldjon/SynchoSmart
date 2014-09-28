package tr.edu.metu.ii.aaa.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;


@SuppressLint("SimpleDateFormat")
public class DateUtils {
    
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    
    public static String convertToDate(long time){
        
        Date date               = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        return format.format(date);
    }
}
