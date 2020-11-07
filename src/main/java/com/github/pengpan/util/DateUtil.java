package com.github.pengpan.util;

import com.alibaba.fastjson.JSON;
import com.github.kevinsawicki.http.HttpRequest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;
import java.util.stream.IntStream;

public class DateUtil {

    public static final String MM_DD = "MM-dd";
    public final static String YYYY_MM_DD = "yyyy-MM-dd";
    public final static String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static Date parse(String date, String pattern) {
        if (date == null) {
            return null;
        }
        if (StringUtil.isEmpty(pattern)) {
            pattern = DEFAULT_PATTERN;
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        try {
            return format.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String format(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        if (StringUtil.isEmpty(pattern)) {
            pattern = DEFAULT_PATTERN;
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    public static String getTimestamp() {
        return getTimestamp(true);
    }

    public static String getTimestamp(boolean toMS) {
        long time = new Date().getTime();
        return String.valueOf(toMS ? time : (time / 1000));
    }

    /**
     * 获取未来几个工作日（不含当天）
     */
    public static String getWorkDays(int n) {
        LocalDate now = LocalDate.now();
        return IntStream.iterate(1, i -> ++i)
                .mapToObj(now::plusDays)
                .filter(DateUtil::isWorkDay)
                .limit(n)
                .sorted(Comparator.reverseOrder())
                .map(x -> x.format(DateTimeFormatter.ofPattern(DateUtil.YYYY_MM_DD)))
                .findFirst().orElse(null);
    }

    public static boolean isWorkDay(LocalDate localDate) {
        return !isWeekDay(localDate);
    }

    public static boolean isWeekDay(LocalDate localDate) {
        String date = localDate.format(DateTimeFormatter.ofPattern(DateUtil.YYYY_MM_DD));
        String url = String.format("http://timor.tech/api/holiday/info/%s", date);
        String body = HttpRequest.get(url).userAgent("Mozilla/5.0").body();

        // 节假日类型，分别表示 0：工作日、1：周末、2：节日、3：调休。
        Integer type = Optional.ofNullable(body)
                .map(JSON::parseObject)
                .map(x -> x.getJSONObject("type"))
                .map(x -> x.getInteger("type"))
                .orElse(null);
        if (type != null) {
            return Arrays.asList(1, 2).contains(type);
        }

        DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    public static void main(String[] args) {
        System.out.println(getWorkDays(3));
    }
}
