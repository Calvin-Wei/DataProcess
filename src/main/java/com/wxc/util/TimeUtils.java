package com.wxc.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
/**
 * 
 * @author wxc
 *
 */
public class TimeUtils {
    public static final SimpleDateFormat formatYMD = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat formatYMD2 = new SimpleDateFormat("yyyyMMddHHmmss");
    public static final SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
    public static final SimpleDateFormat yearMonth = new SimpleDateFormat("yyyyMM");
    public static final SimpleDateFormat year = new SimpleDateFormat("yyyy");
    public static final SimpleDateFormat month = new SimpleDateFormat("MM");

    public static Timestamp getBeginOfDay() {
        return new Timestamp(truncate(new Date(), Calendar.DATE).getTime());
    }

    public static Timestamp getLastMonth() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        return new Timestamp(truncate(c.getTime(), Calendar.DATE).getTime());
    }

    public static Timestamp getEndOfDay() {
        return new Timestamp(round(new Date(), Calendar.DATE).getTime());
    }

    public static Date truncate(Date date, int field) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        switch (field) {
        case Calendar.YEAR:
            c.set(Calendar.MONTH, 0);
        case Calendar.MONTH:
            c.set(Calendar.DAY_OF_MONTH, 1);
        case Calendar.DATE:
            c.set(Calendar.HOUR_OF_DAY, 0);
        case Calendar.HOUR:
            c.set(Calendar.MINUTE, 0);
        case Calendar.MINUTE:
            c.set(Calendar.SECOND, 0);
        case Calendar.SECOND:
            c.set(Calendar.MILLISECOND, 0);
            break;
        }
        return c.getTime();
    }

    public static Date round(Date date, int field) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        switch (field) {
        case Calendar.YEAR:
            c.set(Calendar.MONTH, 11);
        case Calendar.MONTH:
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        case Calendar.DATE:
            c.set(Calendar.HOUR_OF_DAY, 23);
        case Calendar.HOUR:
            c.set(Calendar.MINUTE, 59);
        case Calendar.MINUTE:
            c.set(Calendar.SECOND, 59);
            break;
        }
        return c.getTime();
    }

    /**转成 yyyyMM 格式
     * @param date
     * @return
     */
    public static Timestamp getYearMonthFormat(String date) {
        Date ymDate = null;
        try {
            ymDate = yearMonth.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Timestamp(ymDate.getTime());
    }

    /**转成 yyyy 格式
     * @param strYear
     * @return
     */
    public static Timestamp getYearFormat(String strYear) {
        Date yearDate = null;
        try {
            yearDate = year.parse(strYear);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Timestamp(yearDate.getTime());
    }

    /**yyyy年MM月dd日
     * @param date
     * @return
     */

    public static String getYMD(Timestamp date) {
        if (date == null)
            return null;
        return format.format(date);
    }

    /**yyyy-MM-dd HH:mm:ss
     * @param date
     * @return
     */
    public static String getNormalTime(Timestamp date) {
        if (date == null)
            return null;
        return formatYMD.format(date);
    }
    
    /**yyyyMMddHHmmss
     * @param date
     * @return
     */
    public static String getNormalTime1(Timestamp date) {
        if (date == null)
            return null;
        return formatYMD2.format(date);
    }

    public static Timestamp normalTime(String date) {
        Timestamp normalTime = null;
        try {
            if (date != null && date.length() > 0)
                normalTime = new Timestamp(formatYMD.parse(date).getTime());
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return normalTime;
    }
    
    public static Timestamp normalTime2(String date) {
        Timestamp normalTime = null;
        try {
            if (date != null && date.length() > 0)
                normalTime = new Timestamp(formatYMD2.parse(date).getTime());
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return normalTime;
    }

    public static Timestamp getTimestamp(Date date) {
        Timestamp normalTime = null;
        if (date != null){
            normalTime = new Timestamp(date.getTime());
        }
        return normalTime;
    }

    /**获取当前year
     * @return
     */
    public static String getYear() {
        return year.format(System.currentTimeMillis());
    }

    /**获取当前month
     * @return
     */
    public static String getMonth() {
        return month.format(System.currentTimeMillis());
    }

    public static Date nowDate() {
        return new Date();
    }

    public static Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }

    public static Timestamp now(long millis) {
        return new Timestamp(System.currentTimeMillis() + millis);
    }

    public static boolean elapsed(Timestamp t, long millis) {
        long current = System.currentTimeMillis();
        long point = current - millis;
        return t.getTime() < point;
    }

    public static boolean sameMonth(Timestamp t1, Timestamp t2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTimeInMillis(t1.getTime());
        c2.setTimeInMillis(t2.getTime());
        return (c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) && c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR));
    }

    public static long costTime(long startTime) {
        return System.currentTimeMillis() - startTime;
    }


}
