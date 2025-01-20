package com.codenumnum.agendabyweather.presentation.domain;

import com.codenumnum.agendabyweather.dao.domain.WeatherForecast;
import com.codenumnum.agendabyweather.dao.domain.jpa.Agenda;

public record AgendaWeatherResponse(Agenda agenda, WeatherForecast weatherForecast) {
}
