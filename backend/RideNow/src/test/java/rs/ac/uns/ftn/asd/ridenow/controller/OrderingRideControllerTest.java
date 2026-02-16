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
// removed unused import
import rs.ac.uns.ftn.asd.ridenow.model.Driver;
import rs.ac.uns.ftn.asd.ridenow.model.RegisteredUser;
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;
import rs.ac.uns.ftn.asd.ridenow.service.RideService;

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
        OrderRideRequestDTO invalidRequest = new OrderRideRequestDTO(); // prazno -> @Valid pali

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
}
