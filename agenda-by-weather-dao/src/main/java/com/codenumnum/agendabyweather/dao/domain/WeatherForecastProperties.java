package com.codenumnum.agendabyweather.dao.domain;

import java.time.OffsetDateTime;
import java.util.List;


public record WeatherForecastProperties(String units, OffsetDateTime generatedAt, OffsetDateTime updateTime,
                                        List<WeatherForecastPeriod> periods) {
}
