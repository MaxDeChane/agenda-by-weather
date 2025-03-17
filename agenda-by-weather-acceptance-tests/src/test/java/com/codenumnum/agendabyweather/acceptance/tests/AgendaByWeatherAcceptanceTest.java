package com.codenumnum.agendabyweather.acceptance.tests;

import com.codenumnum.agendabyweather.AgendaByWeatherApplication;
import com.codenumnum.agendabyweather.dao.domain.WeatherForecastPeriod;
import com.codenumnum.agendabyweather.dao.domain.jpa.AgendaItem;
import com.codenumnum.agendabyweather.service.domain.AgendaDayDto;
import com.codenumnum.agendabyweather.service.domain.AgendaDto;
import com.codenumnum.agendabyweather.service.domain.AgendaItemCrudStatusEnum;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.ResourceUtils;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.UUID;

import static com.ninja_squad.dbsetup.Operations.*;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest(classes = AgendaByWeatherApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AgendaByWeatherAcceptanceTest {

    private static final UUID AGENDA_UUID = UUID.randomUUID();
    private static final UUID AGENDA_ITEM_UUID = UUID.randomUUID();
    // Default testing agenda date which is an hour apart
    private static final OffsetDateTime START_DATE_TIME = OffsetDateTime.of(LocalDateTime.of(2025, 1, 5, 12, 0), ZoneOffset.ofHours(6));
    private static final OffsetDateTime END_DATE_TIME = OffsetDateTime.of(LocalDateTime.of(2025, 1, 5, 13, 0), ZoneOffset.ofHours(6));
    private static final Operation DELETE_ALL = deleteAllFrom("AGENDA_AGENDA_ITEMS", "AGENDA_DAY", "AGENDA", "AGENDA_ITEM");
    private static ClientAndServer mockServer;
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    public static void beforeAll() {
        mockServer = startClientAndServer(8081);
    }

    @AfterAll
    public static void afterAll() {
        mockServer.stop();
    }

    @BeforeEach
    public void beforeEach() {
        Operation operation =
                sequenceOf(
                        DELETE_ALL,
                        insertInto("AGENDA")
                                .columns("ID","DEFAULT_AGENDA", "LAT_LON", "GENERAL_WEATHER_FORECAST_URL", "HOURLY_WEATHER_FORECAST_URL")
                                .values(AGENDA_UUID, true, "test,-LatLon", "http://localhost:8081/generalWeather", "http://localhost:8081/hourlyWeather")
                                .build(),
                        insertInto("AGENDA_ITEM")
                                .columns("ID","NAME", "START_DATE_TIME", "END_DATE_TIME")
                                .values(AGENDA_ITEM_UUID, "testAgendaItem", START_DATE_TIME, END_DATE_TIME)
                                .build(),
                        insertInto("AGENDA_AGENDA_ITEMS")
                                .columns("AGENDA_ID", "AGENDA_ITEMS_ID")
                                .values(AGENDA_UUID, AGENDA_ITEM_UUID)
                                .build());

        new DbSetup(new DataSourceDestination(dataSource), operation).launch();
    }

    @SneakyThrows
    private String getExpectedJsonResponseAsString(String fileName) {
        return Files.readString(Paths.get(ResourceUtils.getURL("classpath:" + fileName + ".json").getFile()));
    }

    @Test
    public void testGetDefaultAgendaWeather_NoDefaultAgendaSet_HttpStatus404() {
        // Delete the db for this test so it acts like it would on first start up.
        new DbSetup(new DataSourceDestination(dataSource), DELETE_ALL).launch();

        webTestClient.get().uri("/agenda-weather")
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @SneakyThrows
    @Test
    public void testGetDefaultAgendaWeather_DefaultAgendaFoundWithWeatherUrls_AgendaWithWeatherAndLatLon() {
        String okGeneralForecastResponseAsString = getExpectedJsonResponseAsString("ok-general-forecast-response");
        String okHourlyForecastResponseAsString = getExpectedJsonResponseAsString("ok-hourly-forecast-response");

        mockServer.when(request().withMethod("GET").withPath("/generalWeather"))
                .respond(response().withStatusCode(200).withContentType(MediaType.APPLICATION_JSON).withBody(okGeneralForecastResponseAsString));

        mockServer.when(request().withMethod("GET").withPath("/hourlyWeather"))
                .respond(response().withStatusCode(200).withContentType(MediaType.APPLICATION_JSON).withBody(okHourlyForecastResponseAsString));

        webTestClient.get().uri("/agenda-weather")
                .exchange()
                .expectStatus().isOk()
                .expectBody(AgendaDto.class).value(actual -> {
                    Assertions.assertNotNull(actual);
                    Assertions.assertEquals("test,-LatLon", actual.latLon());

                    var actualPeriods = actual.agendaDaysByDay().values().stream()
                            .map(AgendaDayDto::generalWeatherPeriods)
                            .flatMap(offsetDateTimeWeatherForecastPeriodMap -> offsetDateTimeWeatherForecastPeriodMap.values().stream())
                            .sorted(Comparator.comparing(WeatherForecastPeriod::startTime))
                            .toList();
                    Assertions.assertEquals("Sunny", actualPeriods.get(0).shortForecast());
                    Assertions.assertEquals("Wednesday Night", actualPeriods.get(13).name());

                    actualPeriods = actual.agendaDaysByDay().values().stream()
                            .map(AgendaDayDto::hourlyWeatherPeriods)
                            .flatMap(offsetDateTimeWeatherForecastPeriodMap -> offsetDateTimeWeatherForecastPeriodMap.values().stream())
                            .sorted(Comparator.comparing(WeatherForecastPeriod::startTime))
                            .toList();

                    Assertions.assertEquals("Sunny", actualPeriods.get(0).shortForecast());
                    Assertions.assertEquals("Slight Chance Light Snow", actualPeriods.get(155).shortForecast());
                });
    }

    @SneakyThrows
    @Test
    public void testUpdateLatLon_EverythingWorks_AgendaWithWeatherAndLatLon() {
        String okGeocodingOneLineAddressResponseAsString = getExpectedJsonResponseAsString("ok-geocoding-onelineaddress-response");
        String okPointsResponseAsString = getExpectedJsonResponseAsString("ok-points-response");
        String okGeneralForecastResponseAsString = getExpectedJsonResponseAsString("ok-general-forecast-response");
        String okHourlyForecastResponseAsString = getExpectedJsonResponseAsString("ok-hourly-forecast-response");

        mockServer.when(request().withMethod("GET").withPath("/onelineaddress").withQueryStringParameter("address", "testAddress"))
                .respond(response().withStatusCode(200).withContentType(MediaType.APPLICATION_JSON).withBody(okGeocodingOneLineAddressResponseAsString));
        mockServer.when(request().withMethod("GET").withPath("/points/43.0740%2C-89.3831"))
                .respond(response().withStatusCode(200).withContentType(MediaType.APPLICATION_JSON).withBody(okPointsResponseAsString));
        mockServer.when(request().withMethod("GET").withPath("/generalWeather"))
                .respond(response().withStatusCode(200).withContentType(MediaType.APPLICATION_JSON).withBody(okGeneralForecastResponseAsString));
        mockServer.when(request().withMethod("GET").withPath("/hourlyWeather"))
                .respond(response().withStatusCode(200).withContentType(MediaType.APPLICATION_JSON).withBody(okHourlyForecastResponseAsString));

        webTestClient.put().uri("/agenda-weather/testAddress")
                .exchange()
                .expectStatus().isOk()
                .expectBody(AgendaDto.class).value(actual -> {
                    Assertions.assertTrue(actual.defaultAgenda());
                    Assertions.assertEquals("43.0740,-89.3831", actual.latLon());

                    var actualPeriods = actual.agendaDaysByDay().values().stream()
                            .map(AgendaDayDto::generalWeatherPeriods)
                            .flatMap(offsetDateTimeWeatherForecastPeriodMap -> offsetDateTimeWeatherForecastPeriodMap.values().stream())
                            .sorted(Comparator.comparing(WeatherForecastPeriod::startTime))
                            .toList();
                    Assertions.assertEquals("Sunny", actualPeriods.get(0).shortForecast());
                    Assertions.assertEquals("Wednesday Night", actualPeriods.get(13).name());

                    actualPeriods = actual.agendaDaysByDay().values().stream()
                            .map(AgendaDayDto::hourlyWeatherPeriods)
                            .flatMap(offsetDateTimeWeatherForecastPeriodMap -> offsetDateTimeWeatherForecastPeriodMap.values().stream())
                            .sorted(Comparator.comparing(WeatherForecastPeriod::startTime))
                            .toList();

                    Assertions.assertEquals("Sunny", actualPeriods.get(0).shortForecast());
                    Assertions.assertEquals("Slight Chance Light Snow", actualPeriods.get(155).shortForecast());
                });
    }

    @Test
    public void testUpdateAgendaItem_UpdateAllFieldsOnExistingItem_AllAgendaItemFieldsUpdated() {
        var newStartDateTime = OffsetDateTime.parse("2024-02-12T10:15:00+01:00");
        var newEndDateTime = OffsetDateTime.parse("2024-02-12T11:15:00+01:00");

        var updateAgendaItem = AgendaItem.builder().name("newItem").startDateTime(newStartDateTime).endDateTime(newEndDateTime).build();

        webTestClient.put().uri("/agenda-weather/test,-LatLon/agenda-item/testAgendaItem")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(updateAgendaItem)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AgendaItemCrudStatusEnum.class)
                .isEqualTo(AgendaItemCrudStatusEnum.UPDATED);

        String whereClause = String.format("ID = '%s' and NAME = 'newItem' and START_DATE_TIME = '%s' and  END_DATE_TIME = '%s'",
                AGENDA_ITEM_UUID, newStartDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), newEndDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        int agendaItemsRowCount = JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "AGENDA_ITEM", whereClause);
        Assertions.assertEquals(1, agendaItemsRowCount);
    }

    @Test
    public void testUpdateAgendaItem_UpdateJustNameOnExistingItem_OnlyItemNameUpdated() {
        var updateAgendaItem = AgendaItem.builder().name("newItem").startDateTime(START_DATE_TIME).endDateTime(END_DATE_TIME).build();

        webTestClient.put().uri("/agenda-weather/test,-LatLon/agenda-item/testAgendaItem")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(updateAgendaItem)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AgendaItemCrudStatusEnum.class)
                .isEqualTo(AgendaItemCrudStatusEnum.UPDATED);

        String whereClause = String.format("ID = '%s' and NAME = 'newItem' and START_DATE_TIME = '%s' and  END_DATE_TIME = '%s'",
                AGENDA_ITEM_UUID, START_DATE_TIME.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), END_DATE_TIME.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        int agendaItemsRowCount = JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "AGENDA_ITEM", whereClause);
        Assertions.assertEquals(1, agendaItemsRowCount);
    }

    @Test
    public void testDeleteAgendaItem_2AgendaItemsInDb_OneAgendaItem() {
        UUID newAgendaItemUuid = UUID.randomUUID();
        Operation operation =
                sequenceOf(
                    insertInto("AGENDA_ITEM")
                            .columns("ID", "NAME", "START_DATE_TIME", "END_DATE_TIME")
                            .values(newAgendaItemUuid, "newItem", START_DATE_TIME, END_DATE_TIME)
                            .build(),
                    insertInto("AGENDA_AGENDA_ITEMS")
                            .columns("AGENDA_ID", "AGENDA_ITEMS_ID")
                            .values(AGENDA_UUID, newAgendaItemUuid)
                            .build());

        new DbSetup(new DataSourceDestination(dataSource), operation).launch();

        webTestClient.delete().uri("/agenda-weather/test,-LatLon/agenda-item/newItem")
                .exchange()
                .expectStatus().isOk()
                .expectBody(AgendaItemCrudStatusEnum.class)
                .isEqualTo(AgendaItemCrudStatusEnum.DELETED);;

        int aaiRowCount = JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "AGENDA_AGENDA_ITEMS", "AGENDA_ITEMS_ID IS NOT NULL");
        int agendaItemsRowCount = JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "AGENDA_ITEM", "NAME <> ''");
        Assertions.assertEquals(1, aaiRowCount);
        Assertions.assertEquals(1, agendaItemsRowCount);
    }
}
