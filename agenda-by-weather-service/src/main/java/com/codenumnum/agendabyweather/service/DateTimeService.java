package com.codenumnum.agendabyweather.service;

import org.springframework.stereotype.Service;

@Service
public class DateTimeService {

    public String retrieveDayPartFromUtcDateTime(String utcDateTime) {
        return utcDateTime.substring(0, 10);
    }
}
