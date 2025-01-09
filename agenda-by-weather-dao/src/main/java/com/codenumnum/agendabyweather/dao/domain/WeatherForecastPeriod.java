package com.codenumnum.agendabyweather.dao.domain;

import java.time.Instant;

public record WeatherForecastPeriod(int number, String name, Instant startTime, Instant endTime, boolean isDaytime) {
}
