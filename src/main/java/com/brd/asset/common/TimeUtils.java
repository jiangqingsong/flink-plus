package com.brd.asset.common;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author leo.J
 * @description
 * @date 2020-05-15 15:40
 */
public class TimeUtils {
    public static long getTimestamp(String pattern, String time) {

        long timestamp;
        try {

            timestamp = (new SimpleDateFormat(pattern)).parse(time, new ParsePosition(0)).getTime();
        }catch (Exception e){
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    /**
     * 时间戳格式化
     *
     * @param timestamp
     * @param pattern
     * @return
     */
    public static String convertTimestamp2Date(Long timestamp, String pattern) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        return simpleDateFormat.format(new Date(timestamp));

    }

    /**
     * 获取当前日期
     *
     * @param pattern
     * @return
     */
    public static String getCurrentDate(String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    /**
     * 获取前几天日期
     *
     * @param gapDays
     * @return
     */
    public static String getPreDate(String date, int gapDays) {
        return String.valueOf(Integer.valueOf(date) - gapDays);
    }

    public static void main(String[] args) {
        System.out.println(getCurrentDate("yyyyMMdd"));
        System.out.println(getPreDate(getCurrentDate("yyyyMMdd"), 1));
        System.out.println(convertTimestamp2Date(1597126260000L, "yyyy-MM-dd hh:mm:ss"));
    }
}
