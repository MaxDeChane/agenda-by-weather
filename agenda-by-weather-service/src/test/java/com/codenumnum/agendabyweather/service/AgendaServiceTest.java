//package com.codenumnum.agendabyweather.service;
//
//import com.codenumnum.agendabyweather.dao.domain.WeatherForecast;
//import com.codenumnum.agendabyweather.dao.domain.WeatherForecastProperties;
//import com.codenumnum.agendabyweather.dao.domain.jpa.Agenda;
//import com.codenumnum.agendabyweather.dao.repository.AgendaItemRepository;
//import com.codenumnum.agendabyweather.dao.repository.AgendaRepository;
//import com.codenumnum.agendabyweather.service.domain.AgendaDto;
//import com.codenumnum.agendabyweather.service.domain.factory.AgendaDayFactory;
//import com.codenumnum.agendabyweather.service.domain.factory.AgendaFactory;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//
//import java.time.OffsetDateTime;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Optional;
//import java.util.UUID;
//
//class AgendaServiceTest {
//
//    private static final UUID AGENDA_ID = UUID.randomUUID();
//    private AgendaService classUnderTest;
//    private AgendaFactory agendaDtoFactoryMock;
//    private AgendaDayFactory agendaDayFactoryMock;
//    private AgendaRepository agendaRepositoryMock;
//    private AgendaItemRepository agendaItemRepositoryMock;
//    private WeatherService weatherServiceMock;
//    private DateTimeService dateTimeServiceMock;
//    private ObjectMapper objectMapperMock;
//
//    private AgendaDto defaultAgendaDto;
//    private Agenda agenda;
//
//    private WeatherForecast weatherForecast;
//    private WeatherForecastProperties weatherProperties;
//
//    @BeforeEach
//    void setUp() {
//        // Setup mocks
//        agendaDtoFactoryMock = Mockito.mock(AgendaFactory.class);
//        agendaDayFactoryMock = Mockito.mock(AgendaDayFactory.class);
//        agendaRepositoryMock = Mockito.mock(AgendaRepository.class);
//        agendaItemRepositoryMock = Mockito.mock(AgendaItemRepository.class);
//        weatherServiceMock = Mockito.mock(WeatherService.class);
//        dateTimeServiceMock = Mockito.mock(DateTimeService.class);
//        objectMapperMock = Mockito.mock(ObjectMapper.class);
//
//        classUnderTest = new AgendaService(agendaRepositoryMock, agendaDtoFactoryMock, agendaDayFactoryMock, agendaItemRepositoryMock, weatherServiceMock, dateTimeServiceMock, objectMapperMock);
//
//        agenda = Agenda.builder().id(AGENDA_ID).generalWeatherForecastUrl("generalWeatherUrl")
//                .hourlyWeatherForecastUrl("hourlyWeatherUrl")
//                .generalWeatherGeneratedAt(OffsetDateTime.parse("2025-03-10T10:00:00Z"))
//                .generalWeatherUpdateTime(OffsetDateTime.parse("2025-03-10T10:00:00Z"))
//                .build();
//        defaultAgendaDto = new AgendaDto(agenda.getLatLon(), agenda.isDefaultAgenda(), agenda.getHourlyWeatherForecastUrl(),
//                agenda.getHourlyWeatherGeneratedAt(), agenda.getHourlyWeatherGeneratedAt(), agenda.getGeneralWeatherForecastUrl(),
//                agenda.getGeneralWeatherGeneratedAt(), agenda.getGeneralWeatherUpdateTime(), new ArrayList<>(), new HashMap<>());
//
//        weatherForecast = new WeatherForecast(new WeatherForecastProperties(null, agenda.getGeneralWeatherGeneratedAt(), agenda.getGeneralWeatherUpdateTime(), new ArrayList<>()));
//    }
//
//    @Test
//    void testUpdateAgendaFoundAndWeatherNotNeedUpdate_ExpectedAgendaReturnedWithWeatherUpdateDtoWeatherInfo() {
//        // Arrange: Mock agenda repository to return a present agenda
//        Mockito.when(agendaRepositoryMock.findByDefaultAgenda(true)).thenReturn(Optional.of(agenda));
//        Mockito.when(agendaDtoFactoryMock.createDtoFromExistingAgenda(agenda, objectMapperMock)).thenReturn(defaultAgendaDto);
//        Mockito.when(weatherServiceMock.retrieveWeatherForecast(Mockito.anyString())).thenReturn(weatherForecast);
//
//        // Act: Call the method
//        AgendaDto result = classUnderTest.updateGeneralAndHourlyForecastsForAgenda(Optional.empty());
//
//        // Assert: Ensure agenda was returned and weather was updated
//        Mockito.verify(agendaRepositoryMock).findByDefaultAgenda(true);
//        Mockito.verify(weatherServiceMock, Mockito.times(2)).retrieveWeatherForecast(Mockito.anyString());
//        Mockito.verify(agendaRepositoryMock).save(agenda);
//        Assertions.assertNotNull(result);
//    }
//
//    @Test
//    void testUpdateAgendaNotFound_ExpectedNewAgendaDtoWeatherInfoCreated() {
//        // Arrange: Mock agenda repository to return an empty result
//        Mockito.when(agendaRepositoryMock.findByDefaultAgenda(true)).thenReturn(Optional.empty());
//
//        // Act: Call the method
//        AgendaDto result = classUnderTest.updateGeneralAndHourlyForecastsForAgenda(Optional.empty());
//
//        // Assert: Ensure new agenda is created
//        Mockito.verify(agendaRepositoryMock).save(Mockito.any());
//        Assertions.assertNotNull(result);
//        Assertions.assertTrue(result.agendaDaysByDay().isEmpty());
//    }
//
//    @Test
//    void testUpdateAgendaCreatingIfNotPresent_GivenWeatherForecastException_ExpectedAgendaReturnedWithoutWeatherUpdateDtoWeatherInfo() {
//        // Arrange: Mock agenda repository to return a present agenda
//        Mockito.when(agendaRepositoryMock.findByDefaultAgenda(true)).thenReturn(Optional.of(agenda));
//        Mockito.when(agendaDtoFactoryMock.createDtoFromExistingAgenda(agenda, objectMapperMock)).thenReturn(defaultAgendaDto);
//        Mockito.when(weatherServiceMock.retrieveWeatherForecast(Mockito.anyString())).thenThrow(new RuntimeException("Weather fetch error"));
//
//        // Act: Call the method
//        AgendaDto result = classUnderTest.updateGeneralAndHourlyForecastsForAgenda(Optional.empty());
//
//        // Assertions.assert: Ensure agenda was returned even if weather fetch failed
//        Mockito.verify(agendaRepositoryMock).findByDefaultAgenda(true);
//        Mockito.verify(weatherServiceMock, Mockito.times(1)).retrieveWeatherForecast(Mockito.anyString());
//        Assertions.assertNotNull(result);
//    }
//
////    @Test
////    void testRetrieveDefaultAgendaCreatingIfNotPresent_GivenNonMatchingWeatherForecastTimes_ExpectedWeatherUpdate() {
////        // Arrange: Create a mock weather forecast and agenda dto
////        WeatherForecast weatherForecast = Mockito.mock(WeatherForecast.class);
////        WeatherForecastProperties properties = Mockito.mock(WeatherForecastProperties.class);
////        Mockito.when(weatherForecast.properties()).thenReturn(properties);
////        Mockito.when(properties.generatedAt()).thenReturn("2025-03-10T10:00:00Z");
////        Mockito.when(properties.updateTime()).thenReturn("2025-03-10T10:00:01Z");
////
////        AgendaDto agendaDto = Mockito.mock(AgendaDto.class);
////        Mockito.when(agendaDto.getGeneratedAt()).thenReturn("2025-03-10T10:00:00Z");
////        Mockito.when(agendaDto.getUpdateTime()).thenReturn("2025-03-10T10:00:00Z");
////
////        // Act: Call the private method using reflection
////        classUnderTest.updateWeatherOnAgenda(agendaDto, weatherForecast, true);
////
////        // Assert: Ensure weather update logic is called when times don't match
////        Mockito.verify(agendaDto, Mockito.times(1)).agendaDaysByDay();
////    }
//}