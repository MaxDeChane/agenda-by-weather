package com.codenumnum.agendabyweather.dao.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class WeatherForecastPeriodTest {

    @Test
    void archivePeriod_TodayEarlyMorningPeriod_ReturnsUpdatedWithEarlyToday() {
        String startTime = LocalDate.now() + "T00:00:00Z";
        WeatherForecastPeriod period = createTestPeriod("Original Name", startTime, startTime, true);

        WeatherForecastPeriod archived = period.archiveGeneralWeatherPeriod();

        assertEquals("Earlier Today", archived.name());
    }

    @Test
    void archivePeriod_TodayEarlyNightPeriod_ReturnsUpdatedWithEarlierTonight() {
        String startTime = LocalDate.now() + "T05:00:00Z";
        WeatherForecastPeriod period = createTestPeriod("Original Name", startTime, startTime, false);

        WeatherForecastPeriod archived = period.archiveGeneralWeatherPeriod();

        assertEquals("Last Night", archived.name());
    }

    @Test
    void archivePeriod_YesterdayDayPeriod_ReturnsUpdatedWithYesterday() {
        String startTime = LocalDate.now().minusDays(1) + "T12:00:00Z";
        WeatherForecastPeriod period = createTestPeriod("Original Name", startTime, startTime, true);

        WeatherForecastPeriod archived = period.archiveGeneralWeatherPeriod();

        assertEquals("Yesterday", archived.name());
    }

    @Test
    void archivePeriod_YesterdayNightPeriod_ReturnsUpdatedWithLastNight() {
        String startTime = LocalDate.now().minusDays(1) + "T23:00:00Z";
        String endTime = LocalDate.now() + "T05:59:59Z";
        WeatherForecastPeriod period = createTestPeriod("Original Name", startTime, endTime, false);

        WeatherForecastPeriod archived = period.archiveGeneralWeatherPeriod();

        assertEquals("Last Night", archived.name());
    }

    @Test
    void archivePeriod_OlderThanYesterdayDayPeriod_ReturnsUpdatedWithDateAndDay() {
        String startTime = LocalDate.now().minusDays(3) + "T14:00:00Z";
        WeatherForecastPeriod period = createTestPeriod("Original Name", startTime, startTime, true);

        WeatherForecastPeriod archived = period.archiveGeneralWeatherPeriod();

        assertEquals(LocalDate.now().minusDays(3).format(DateTimeFormatter.ISO_LOCAL_DATE) + " Day", archived.name());
    }

    @Test
    void archivePeriod_OlderThanYesterdayNightPeriod_ReturnsUpdatedWithDateAndNight() {
        String startTime = LocalDate.now().minusDays(3) + "T22:00:00Z";
        WeatherForecastPeriod period = createTestPeriod("Original Name", startTime, startTime,false);

        WeatherForecastPeriod archived = period.archiveGeneralWeatherPeriod();

        assertEquals(LocalDate.now().minusDays(3).format(DateTimeFormatter.ISO_LOCAL_DATE) + " Night", archived.name());
    }

    @Test
    void archivePeriod_FuturePeriod_ReturnsSamePeriod() {
        String startTime = LocalDate.now().plusDays(1) + "T12:00:00Z";
        WeatherForecastPeriod period = createTestPeriod("Future Name", startTime, startTime,true);

        WeatherForecastPeriod archived = period.archiveGeneralWeatherPeriod();

        assertSame(period, archived);
    }

    private WeatherForecastPeriod createTestPeriod(String name, String startTime, String endTime, boolean isDaytime) {
        return new WeatherForecastPeriod(1, name, OffsetDateTime.parse(startTime), OffsetDateTime.parse(endTime),
                isDaytime, 70, "F", null, null, null,
                null, "10 mph", "N", "Sunny", "Clear skies");
    }
}