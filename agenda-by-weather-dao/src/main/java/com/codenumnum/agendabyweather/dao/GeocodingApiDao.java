package com.codenumnum.agendabyweather.dao;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
public class GeocodingApiDao {

    WebClient webClient;

    public GeocodingApiDao(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://geocoding.geo.census.gov/geocoder/locations/onelineaddress?address={address}&benchmark=4&format=json")
                .build();
    }

    public String retrieveLatLonFromAddress(String address) {
        Map<String, Object> response =  webClient.get()
                .uri(uriBuilder -> uriBuilder.build(address))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        // Dig down to grab the first address match lat lon
        var result = (Map<String, Object>) response.get("result");
        var firstAddressMatch = (Map<String, Object>) ((List)result.get("addressMatches")).get(0);
        var coordinates = (Map<String, Object>) firstAddressMatch.get("coordinates");

        return String.format("%.4f,%.4f", (double) coordinates.get("y"), (double) coordinates.get("x"));
    }
}
