package com.codenumnum.agendabyweather.dao.domain;

import java.time.Instant;
import java.util.List;


public record WeatherForecastProperties(String units, Instant generatedAt, Instant updateTime,
                                        List<WeatherForecastPeriod> periods) {
}
