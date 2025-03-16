package com.codenumnum.agendabyweather.service;

import com.codenumnum.agendabyweather.dao.WeatherApiDao;
import com.codenumnum.agendabyweather.dao.domain.WeatherForecastPeriod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

class WeatherServiceTest {

    private final LocalDate dateDay10 = LocalDate.parse("2025-03-10");
    private final LocalDate dateDay11 = LocalDate.parse("2025-03-11");

    private final OffsetDateTime _2025_03_10T10_00_00Z = OffsetDateTime.parse("2025-03-10T10:00:00Z");
    private final OffsetDateTime _2025_03_10T14_00_00Z = OffsetDateTime.parse("2025-03-10T14:00:00Z");
    private final OffsetDateTime _2025_03_11T10_00_00Z = OffsetDateTime.parse("2025-03-11T10:00:00Z");
    private final OffsetDateTime _2025_03_11T15_00_00Z = OffsetDateTime.parse("2025-03-11T15:00:00Z");

    private WeatherService classUnderTest;

    private WeatherForecastPeriod forecastPeriod1, forecastPeriod2, forecastPeriod3, forecastPeriod4;

    @BeforeEach
    void setUp() {
        DateTimeService dateTimeServiceMock = Mockito.mock(DateTimeService.class);
        WeatherApiDao weatherApiDaoMock = Mockito.mock(WeatherApiDao.class);

        classUnderTest = new WeatherService(dateTimeServiceMock, weatherApiDaoMock);
        // Setup mock WeatherForecastPeriod objects
        forecastPeriod1 = Mockito.mock(WeatherForecastPeriod.class);
        forecastPeriod2 = Mockito.mock(WeatherForecastPeriod.class);
        forecastPeriod3 = Mockito.mock(WeatherForecastPeriod.class);
        forecastPeriod4 = Mockito.mock(WeatherForecastPeriod.class);

        Mockito.when(forecastPeriod1.startTime()).thenReturn(_2025_03_10T10_00_00Z);
        Mockito.when(forecastPeriod2.startTime()).thenReturn(_2025_03_10T14_00_00Z);
        Mockito.when(forecastPeriod3.startTime()).thenReturn(_2025_03_11T10_00_00Z);
        Mockito.when(forecastPeriod4.startTime()).thenReturn(_2025_03_11T15_00_00Z);
    }

    @Test
    void testMapWeatherPeriodsByDay_GivenMultipleForecastPeriods_ExpectedCorrectGroupingByDay() {
        // Arrange: List of forecast periods
        List<WeatherForecastPeriod> weatherForecastPeriods = Arrays.asList(forecastPeriod1, forecastPeriod2, forecastPeriod3, forecastPeriod4);

        // Act: Call the method to map weather periods by day
        Map<LocalDate, Map<OffsetDateTime, WeatherForecastPeriod>> result = classUnderTest.mapWeatherPeriodsByDay(weatherForecastPeriods);

        // Assert: Verify that periods are grouped by day
        Assertions.assertEquals(2, result.size());  // Should have two days
        Assertions.assertTrue(result.containsKey(dateDay10));
        Assertions.assertTrue(result.containsKey(dateDay11));

        Map<OffsetDateTime, WeatherForecastPeriod> day10Periods = result.get(dateDay10);
        Map<OffsetDateTime, WeatherForecastPeriod> day11Periods = result.get(dateDay11);

        Assertions.assertEquals(2, day10Periods.size()); // Two periods on 2025-03-10
        Assertions.assertEquals(2, day11Periods.size()); // Two periods on 2025-03-11

        // Verify that the forecast periods are correctly assigned to the appropriate day
        Assertions.assertTrue(day10Periods.containsKey(_2025_03_10T10_00_00Z));
        Assertions.assertTrue(day10Periods.containsKey(_2025_03_10T14_00_00Z));
        Assertions.assertTrue(day11Periods.containsKey(_2025_03_11T10_00_00Z));
        Assertions.assertTrue(day11Periods.containsKey(_2025_03_11T15_00_00Z));
    }

    @Test
    void testMapWeatherPeriodsByDay_GivenEmptyForecastPeriodsList_ExpectedEmptyResult() {
        // Arrange: Empty list
        List<WeatherForecastPeriod> weatherForecastPeriods = new ArrayList<>();

        // Act: Call the method with an empty list
        Map<LocalDate, Map<OffsetDateTime, WeatherForecastPeriod>> result = classUnderTest.mapWeatherPeriodsByDay(weatherForecastPeriods);

        // Assert: Ensure the result is an empty map
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void testMapWeatherPeriodsByDay_GivenForecastPeriodsForSingleDay_ExpectedSingleGrouping() {
        // Arrange: List with only one day
        List<WeatherForecastPeriod> weatherForecastPeriods = Arrays.asList(forecastPeriod1, forecastPeriod2);

        // Act: Call the method
        Map<LocalDate, Map<OffsetDateTime, WeatherForecastPeriod>> result = classUnderTest.mapWeatherPeriodsByDay(weatherForecastPeriods);

        // Assertions.assert: Ensure that all periods are grouped under the same day
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.containsKey(dateDay10));
        Assertions.assertEquals(2, result.get(dateDay10).size());
    }

    @Test
    void testMapWeatherPeriodsByDay_GivenForecastPeriodsAcrossMultipleDays_ExpectedSeparateGroupingsByDay() {
        // Arrange: Forecast periods across multiple days
        List<WeatherForecastPeriod> weatherForecastPeriods = Arrays.asList(forecastPeriod1, forecastPeriod3);

        // Act: Call the method
        Map<LocalDate, Map<OffsetDateTime, WeatherForecastPeriod>> result = classUnderTest.mapWeatherPeriodsByDay(weatherForecastPeriods);

        // Assert: Ensure two separate days are created
        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.containsKey(dateDay10));
        Assertions.assertTrue(result.containsKey(dateDay11));
    }
}