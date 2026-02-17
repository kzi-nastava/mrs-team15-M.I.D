package rs.ac.uns.ftn.asd.ridenow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.OrderRideRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.OrderRideResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.Driver;
import rs.ac.uns.ftn.asd.ridenow.model.RegisteredUser;
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;
import rs.ac.uns.ftn.asd.ridenow.service.RideService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderingRideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RideService rideService;

    private OrderRideRequestDTO requestDto;
    private OrderRideResponseDTO responseDto;
    private RegisteredUser  registeredUser;
    private UsernamePasswordAuthenticationToken authToken;

    @BeforeEach
    void setUp() {
        requestDto = new OrderRideRequestDTO();
        requestDto.setVehicleType("STANDARD");
        requestDto.setStartAddress("Start A");
        requestDto.setEndAddress("End B");
        requestDto.setStartLatitude(45.0);
        requestDto.setStartLongitude(19.0);
        requestDto.setEndLatitude(45.1);
        requestDto.setEndLongitude(19.1);
        requestDto.setDistanceKm(5.0);
        requestDto.setEstimatedTimeMinutes(10);
        requestDto.setPriceEstimate(100.0);
        requestDto.setRouteLattitudes(List.of(45.0, 45.1));
        requestDto.setRouteLongitudes(List.of(19.0, 19.1));

        responseDto = new OrderRideResponseDTO();
        responseDto.setDriverId(123L);
        responseDto.setMainPassengerEmail("user@gmail.com");
        responseDto.setStartAddress(requestDto.getStartAddress());
        responseDto.setEndAddress(requestDto.getEndAddress());



        registeredUser = new RegisteredUser();
        registeredUser.setId(1L);
        registeredUser.setEmail("user@gmail.com");
        registeredUser.setJwtTokenValid(true);
        registeredUser.setRole(UserRoles.USER);

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        authToken = new UsernamePasswordAuthenticationToken(registeredUser, null, List.of(authority));
    }

    // ============= POSITIVE TESTS ================

    @Test
    @DisplayName("Should return 201 Created with correct JSON structure")
    @WithMockUser(roles = "USER")
    void orderRideReturnsCreated() throws Exception {
        when(rideService.orderRide(any(OrderRideRequestDTO.class), eq(registeredUser.getEmail()))).thenReturn(responseDto);

        mockMvc.perform(post("/api/rides/order-ride").with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.driverId").value(123L))
                .andExpect(jsonPath("$.mainPassengerEmail").value("user@gmail.com"))
                .andExpect(jsonPath("$.startAddress").value("Start A"))
                .andExpect(jsonPath("$.endAddress").value("End B"));

        verify(rideService, times(1)).orderRide(any(OrderRideRequestDTO.class), any(String.class));
    }

    // ============= NEGATIVE TESTS ================

    @Test
    @DisplayName("Should return 403 Forbidden when user is not authenticated")
    void orderRideReturns403WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/rides/order-ride")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());

        verify(rideService, never()).orderRide(any(), any());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when request is invalid")
    @WithMockUser(roles = "USER")
    void orderRideReturns400WhenRequestInvalid() throws Exception {
        OrderRideRequestDTO invalidRequest = new OrderRideRequestDTO();

        mockMvc.perform(post("/api/rides/order-ride").with(authentication(authToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());

        verify(rideService, never()).orderRide(any(), any());
    }

    @Test
    @DisplayName("Should handle service exception gracefully")
    @WithMockUser(roles = "USER")
    void orderRideHandlesServiceException() throws Exception {
        when(rideService.orderRide(any(), any())).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/api/rides/order-ride").with(authentication(authToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Service error"));

        verify(rideService, times(1)).orderRide(any(), any());
    }

    // ============= AUTHORIZATION TESTS ================

    @Test
    @DisplayName("Should return 403 Forbidden when ADMIN tries to order ride")
    void orderRideReturns403WhenAdminRole() throws Exception {
        RegisteredUser adminUser = new RegisteredUser();
        adminUser.setId(5L);
        adminUser.setEmail("admin@gmail.com");
        adminUser.setRole(UserRoles.ADMIN);
        adminUser.setJwtTokenValid(true);

        SimpleGrantedAuthority adminAuthority = new SimpleGrantedAuthority("ROLE_ADMIN");
        UsernamePasswordAuthenticationToken adminToken = 
                new UsernamePasswordAuthenticationToken(adminUser, null, List.of(adminAuthority));

        mockMvc.perform(post("/api/rides/order-ride").with(authentication(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());

        verify(rideService, never()).orderRide(any(), any());
    }

    @Test
    @DisplayName("Should forbid DRIVER from ordering ride")
    void orderRideForbidsDriverRole() throws Exception {
        Driver driverUser = new Driver();
        driverUser.setId(6L);
        driverUser.setEmail("driver@gmail.com");
        driverUser.setRole(UserRoles.DRIVER);
        driverUser.setJwtTokenValid(true);

        SimpleGrantedAuthority driverAuthority = new SimpleGrantedAuthority("ROLE_DRIVER");
        UsernamePasswordAuthenticationToken driverToken = 
                new UsernamePasswordAuthenticationToken(driverUser, null, List.of(driverAuthority));

        mockMvc.perform(post("/api/rides/order-ride").with(authentication(driverToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());

        verify(rideService, never()).orderRide(any(), any());
    }

    // ============= DTO VALIDATION TESTS ================

    @Test
    @DisplayName("Should return 400 when vehicle type is null")
    @WithMockUser(roles = "USER")
    void orderRideReturns400WhenVehicleTypeNull() throws Exception {
        requestDto.setVehicleType(null);

        mockMvc.perform(post("/api/rides/order-ride").with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(rideService, never()).orderRide(any(), any());
    }

    @Test
    @DisplayName("Should return 400 when start address is null")
    @WithMockUser(roles = "USER")
    void orderRideReturns400WhenStartAddressNull() throws Exception {
        requestDto.setStartAddress(null);

        mockMvc.perform(post("/api/rides/order-ride").with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(rideService, never()).orderRide(any(), any());
    }

    @Test
    @DisplayName("Should return 400 when end address is null")
    @WithMockUser(roles = "USER")
    void orderRideReturns400WhenEndAddressNull() throws Exception {
        requestDto.setEndAddress(null);

        mockMvc.perform(post("/api/rides/order-ride").with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(rideService, never()).orderRide(any(), any());
    }

    @Test
    @DisplayName("Should return 400 when coordinates are null")
    @WithMockUser(roles = "USER")
    void orderRideReturns400WhenCoordinatesNull() throws Exception {
        requestDto.setStartLatitude(null);
        requestDto.setStartLongitude(null);

        mockMvc.perform(post("/api/rides/order-ride").with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(rideService, never()).orderRide(any(), any());
    }

    @Test
    @DisplayName("Should return 400 when distance is negative")
    @WithMockUser(roles = "USER")
    void orderRideReturns400WhenDistanceNegative() throws Exception {
        requestDto.setDistanceKm(-5.0);

        mockMvc.perform(post("/api/rides/order-ride").with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(rideService, never()).orderRide(any(), any());
    }

    @Test
    @DisplayName("Should return 400 when price is negative")
    @WithMockUser(roles = "USER")
    void orderRideReturns400WhenPriceNegative() throws Exception {
        requestDto.setPriceEstimate(-100.0);

        mockMvc.perform(post("/api/rides/order-ride").with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(rideService, never()).orderRide(any(), any());
    }

    @Test
    @DisplayName("Should return 400 when estimated time is negative")
    @WithMockUser(roles = "USER")
    void orderRideReturns400WhenEstimatedTimeNegative() throws Exception {
        requestDto.setEstimatedTimeMinutes(-10);

        mockMvc.perform(post("/api/rides/order-ride").with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(rideService, never()).orderRide(any(), any());
    }

    // ============= EDGE CASE TESTS ================

    @Test
    @DisplayName("Should handle scheduled ride request")
    @WithMockUser(roles = "USER")
    void orderRideHandlesScheduledTime() throws Exception {
        LocalDateTime scheduledTime = LocalDateTime.now().plusHours(2);
        requestDto.setScheduledTime(scheduledTime);
        responseDto.setScheduledTime(scheduledTime);

        when(rideService.orderRide(any(OrderRideRequestDTO.class), eq(registeredUser.getEmail()))).thenReturn(responseDto);

        mockMvc.perform(post("/api/rides/order-ride").with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.driverId").value(123L))
                .andExpect(jsonPath("$.scheduledTime").exists());

        verify(rideService, times(1)).orderRide(any(), any());
    }

    @Test
    @DisplayName("Should handle ride request with linked passengers")
    @WithMockUser(roles = "USER")
    void orderRideHandlesLinkedPassengers() throws Exception {
        requestDto.setLinkedPassengers(List.of("passenger1@gmail.com", "passenger2@gmail.com"));
        responseDto.setLinkedPassengers(requestDto.getLinkedPassengers());

        when(rideService.orderRide(any(OrderRideRequestDTO.class), eq(registeredUser.getEmail()))).thenReturn(responseDto);

        mockMvc.perform(post("/api/rides/order-ride").with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.driverId").value(123L))
                .andExpect(jsonPath("$.linkedPassengers").isArray())
                .andExpect(jsonPath("$.linkedPassengers.length()").value(2));

        verify(rideService, times(1)).orderRide(any(), any());
    }

    @Test
    @DisplayName("Should handle ride request with baby and pet friendly options")
    @WithMockUser(roles = "USER")
    void orderRideHandlesBabyAndPetFriendly() throws Exception {
        requestDto.setBabyFriendly(true);
        requestDto.setPetFriendly(true);

        when(rideService.orderRide(any(OrderRideRequestDTO.class), eq(registeredUser.getEmail()))).thenReturn(responseDto);

        mockMvc.perform(post("/api/rides/order-ride").with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.driverId").value(123L));

        verify(rideService, times(1)).orderRide(any(), any());
    }

    @Test
    @DisplayName("Should handle empty linked passengers list")
    @WithMockUser(roles = "USER")
    void orderRideHandlesEmptyLinkedPassengers() throws Exception {
        requestDto.setLinkedPassengers(List.of());

        when(rideService.orderRide(any(OrderRideRequestDTO.class), eq(registeredUser.getEmail()))).thenReturn(responseDto);

        mockMvc.perform(post("/api/rides/order-ride").with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.driverId").value(123L));

        verify(rideService, times(1)).orderRide(any(), any());
    }
}
