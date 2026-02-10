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
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.StopRideResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.Driver;
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;
import rs.ac.uns.ftn.asd.ridenow.service.RideService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class StopRideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RideService rideService;

    @Autowired
    private ObjectMapper objectMapper;

    private Driver driver;
    private StopRideResponseDTO success;
    private UsernamePasswordAuthenticationToken authToken;

    @BeforeEach
    void setUp() {
        driver = new Driver();
        driver.setId(1L);
        driver.setEmail("driver@gmail.com");
        driver.setJwtTokenValid(true);
        driver.setRole(UserRoles.DRIVER);

        success = new StopRideResponseDTO();
        success.setPrice(290.0);
        success.setDistanceKm(1.7);
        success.setEstimatedDurationMin(6);
        success.setEndAddress("Zlatne grede 4, Novi Sad");

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_DRIVER");
        authToken = new UsernamePasswordAuthenticationToken(driver, null, List.of(authority));
    }

    // ============= POSITIVE TESTS ================

    @Test
    @DisplayName("Should return correct JSON structure")
    @WithMockUser(roles = "DRIVER")
    void stopRideReturnsCorrectJsonStructure() throws Exception {
        when(rideService.stopRide(driver)).thenReturn(success);

        mockMvc.perform(put("/api/rides/stop").with(authentication(authToken)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.price").exists())
                .andExpect(jsonPath("$.distanceKm").exists())
                .andExpect(jsonPath("$.estimatedDurationMin").exists())
                .andExpect(jsonPath("$.endAddress").exists());

        verify(rideService, times(1)).stopRide(driver);
    }

    @Test
    @DisplayName("Should return 200 OK when driver successfully stops ride")
    @WithMockUser("DRIVER")
    void stopRideReturns200WhenSuccessful() throws Exception {
        when(rideService.stopRide(driver)).thenReturn(success);

        mockMvc.perform(put("/api/rides/stop").with(authentication(authToken)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.price").value(290.0))
                .andExpect(jsonPath("$.distanceKm").value(1.7))
                .andExpect(jsonPath("$.estimatedDurationMin").value(6))
                .andExpect(jsonPath("$.endAddress").value("Zlatne grede 4, Novi Sad"));

        verify(rideService, times(1)).stopRide(driver);
    }

    // ============= NEGATIVE TESTS ================

    @Test
    @DisplayName("Should return 400 Bad Request when driver has no active ride")
    @WithMockUser(roles = "DRIVER")
    void stopRideReturns400WhenNoActiveRide() throws Exception {
        when(rideService.stopRide(driver)).thenThrow(new Exception("You don't have a ride in progress."));

        mockMvc.perform(put("/api/rides/stop").with(authentication(authToken)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("You don't have a ride in progress."));

        verify(rideService, times(1)).stopRide(driver);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when user is not a driver")
    @WithMockUser(roles = "USER")
    void stopRideReturns400WhenUserIsNotDriver() throws Exception {
        when(rideService.stopRide(any())).thenThrow(new Exception("You are not a driver."));

        mockMvc.perform(put("/api/rides/stop").with(authentication(authToken)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("You are not a driver."));

        verify(rideService, times(1)).stopRide(driver);
    }

    @Test
    @DisplayName("Should return 403 Forbidden when user is not authenticated")
    void stopRideReturns403WhenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/api/rides/stop").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        verify(rideService, never()).startRide(any());
    }

    @Test
    @DisplayName("Should return 400 when service throws generic exception")
    @WithMockUser(roles = "DRIVER")
    void stopRideReturns400WhenServiceThrowsException() throws Exception {
        when(rideService.stopRide(any())).thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(put("/api/rides/stop").with(authentication(authToken)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Database connection failed"));

        verify(rideService, times(1)).stopRide(driver);
    }
}
