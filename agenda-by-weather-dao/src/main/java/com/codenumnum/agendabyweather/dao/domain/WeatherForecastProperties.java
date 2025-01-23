package com.codenumnum.agendabyweather.dao.domain;

import java.util.List;


public record WeatherForecastProperties(String units, String generatedAt, String updateTime,
                                        List<WeatherForecastPeriod> periods) {
}
