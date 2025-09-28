package com.thirdeye3.stockviewer.utils;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.thirdeye3.stockviewer.services.PropertyService;

@Component
public class TimeManager {
    
    @Value("${thirdeye.timezone}")
    private String timeZone;
    
    @Value("${thirdeye.timebufferinseconds}")
    private Long timeBufferInSeconds;
    
    @Autowired
    PropertyService propertyService;

    public Timestamp getCurrentTime() {
        ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of(timeZone));
        LocalDateTime localDateTime = currentTime.toLocalDateTime();
        return Timestamp.valueOf(localDateTime);
    }
    
    public long getMillisUntilNextMinute() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of(timeZone));
        LocalDateTime nextMinute = now.plusMinutes(1).withSecond(0).withNano(0);
        return ChronoUnit.MILLIS.between(now, nextMinute);
    }
    
    public long getMinutesGapBetweenTime(Timestamp t1, Timestamp t2)
    {
    	long diffMillis = t1.getTime() - t2.getTime();
        long diffMinutes = diffMillis / (60 * 1000);
        return diffMinutes;
    }
    
    public int getMinute(Timestamp t)
    {
    	LocalDateTime localTime = t.toLocalDateTime();
        return localTime.getMinute();
    }
    
    public boolean allowPriceUpdate() {
        ZonedDateTime adjustedTime = ZonedDateTime.now(ZoneId.of(timeZone)).minusSeconds(timeBufferInSeconds);
        DayOfWeek day = adjustedTime.getDayOfWeek();
        boolean isWeekday = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
        LocalTime currentLocalTime = adjustedTime.toLocalTime();
        boolean isWithinMorningHours =
                !currentLocalTime.isBefore(propertyService.getMorningPriceUpdaterStartTime()) &&
                !currentLocalTime.isAfter(propertyService.getMorningPriceUpdaterEndTime());
        boolean isWithinEveningHours =
                !currentLocalTime.isBefore(propertyService.getEveningPriceUpdaterStartTime()) &&
                !currentLocalTime.isAfter(propertyService.getEveningPriceUpdaterEndTime());
        return isWeekday && (isWithinMorningHours || isWithinEveningHours);
    }
    
    
    public boolean isBetweenMorningEndAndEveningEnd() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(timeZone));
        DayOfWeek day = now.getDayOfWeek();
        boolean isWeekday = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
        LocalTime currentLocalTime = now.toLocalTime();
        LocalTime morningEnd = propertyService.getMorningPriceUpdaterEndTime();
        LocalTime eveningEnd = propertyService.getEveningPriceUpdaterEndTime();
        return isWeekday && currentLocalTime.isAfter(morningEnd) && currentLocalTime.isBefore(eveningEnd);
    }





}

