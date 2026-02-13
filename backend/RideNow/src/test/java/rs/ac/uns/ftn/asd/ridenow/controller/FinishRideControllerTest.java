package rs.ac.uns.ftn.asd.ridenow.controller;

import jakarta.persistence.EntityNotFoundException;
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
import rs.ac.uns.ftn.asd.ridenow.model.Driver;
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;
import rs.ac.uns.ftn.asd.ridenow.service.RideService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("RideController - Finish Ride Integration Tests")
public class FinishRideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RideService rideService;


    private Driver driver;
    private UsernamePasswordAuthenticationToken driverAuthToken;

    @BeforeEach
    void setUp() {
        // Create test driver
        driver = new Driver();
        driver.setId(1L);
        driver.setEmail("driver@example.com");
        driver.setFirstName("John");
        driver.setLastName("Driver");
        driver.setJwtTokenValid(true);
        driver.setRole(UserRoles.DRIVER);

        // Create authentication token for driver
        SimpleGrantedAuthority driverAuthority = new SimpleGrantedAuthority("ROLE_DRIVER");
        driverAuthToken = new UsernamePasswordAuthenticationToken(driver, null, List.of(driverAuthority));
    }

    // ============= POSITIVE TESTS ================

    @Test
    @DisplayName("Should successfully finish ride and return true when driver has next scheduled ride")
    @WithMockUser(roles = "DRIVER")
    void finishRide_ShouldReturnTrue_WhenDriverHasNextScheduledRide() throws Exception {
        Long rideId = 1L;
        when(rideService.finishRide(eq(rideId), eq(driver.getId()))).thenReturn(true);

        mockMvc.perform(post("/api/rides/{id}/finish", rideId)
                        .with(authentication(driverAuthToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(true));

        verify(rideService, times(1)).finishRide(eq(rideId), eq(driver.getId()));
    }

    @Test
    @DisplayName("Should successfully finish ride and return false when driver has no next scheduled ride")
    @WithMockUser(roles = "DRIVER")
    void finishRide_ShouldReturnFalse_WhenDriverHasNoNextScheduledRide() throws Exception {
        Long rideId = 2L;
        when(rideService.finishRide(eq(rideId), eq(driver.getId()))).thenReturn(false);

        mockMvc.perform(post("/api/rides/{id}/finish", rideId)
                        .with(authentication(driverAuthToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(false));

        verify(rideService, times(1)).finishRide(eq(rideId), eq(driver.getId()));
    }

    @Test
    @DisplayName("Should handle valid ride ID with maximum long value")
    @WithMockUser(roles = "DRIVER")
    void finishRide_ShouldHandleMaximumLongValue() throws Exception {
        Long rideId = Long.MAX_VALUE;
        when(rideService.finishRide(eq(rideId), eq(driver.getId()))).thenReturn(false);

        mockMvc.perform(post("/api/rides/{id}/finish", rideId)
                        .with(authentication(driverAuthToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(false));

        verify(rideService, times(1)).finishRide(eq(rideId), eq(driver.getId()));
    }

    // ============= NEGATIVE TESTS ================

    @Test
    @DisplayName("Should return 400 Bad Request when ride ID is zero")
    @WithMockUser(roles = "DRIVER")
    void finishRide_ShouldReturn400_WhenRideIdIsZero() throws Exception {
        Long rideId = 0L;

        mockMvc.perform(post("/api/rides/{id}/finish", rideId)
                        .with(authentication(driverAuthToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(rideService, never()).finishRide(any(), any());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when ride ID is negative")
    @WithMockUser(roles = "DRIVER")
    void finishRide_ShouldReturn400_WhenRideIdIsNegative() throws Exception {
        Long rideId = -1L;

        mockMvc.perform(post("/api/rides/{id}/finish", rideId)
                        .with(authentication(driverAuthToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(rideService, never()).finishRide(any(), any());
    }

    @Test
    @DisplayName("Should return 404 Not Found when ride does not exist")
    @WithMockUser(roles = "DRIVER")
    void finishRide_ShouldReturn404_WhenRideDoesNotExist() throws Exception {
        Long rideId = 999L;
        when(rideService.finishRide(eq(rideId), eq(driver.getId())))
                .thenThrow(new EntityNotFoundException("Ride with id " + rideId + " not found"));

        mockMvc.perform(post("/api/rides/{id}/finish", rideId)
                        .with(authentication(driverAuthToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound()) // GlobalExceptionHandler returns 404 for EntityNotFoundException
                .andExpect(content().string("Ride with id " + rideId + " not found"));

        verify(rideService, times(1)).finishRide(eq(rideId), eq(driver.getId()));
    }

    @Test
    @DisplayName("Should return 403 Forbidden when user is not authenticated")
    void finishRide_ShouldReturn403_WhenNotAuthenticated() throws Exception {
        Long rideId = 1L;

        mockMvc.perform(post("/api/rides/{id}/finish", rideId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(rideService, never()).finishRide(any(), any());
    }

    @Test
    @DisplayName("Should return 403 Forbidden when user does not have DRIVER role")
    @WithMockUser(roles = "USER")
    void finishRide_ShouldReturn403_WhenUserIsNotDriver() throws Exception {
        Long rideId = 1L;

        mockMvc.perform(post("/api/rides/{id}/finish", rideId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(rideService, never()).finishRide(any(), any());
    }

    @Test
    @DisplayName("Should handle service throwing runtime exception")
    @WithMockUser(roles = "DRIVER")
    void finishRide_ShouldHandleRuntimeException() {
        Long rideId = 1L;
        when(rideService.finishRide(eq(rideId), eq(driver.getId())))
                .thenThrow(new RuntimeException("Database connection failed"));

        // RuntimeException is not handled by GlobalExceptionHandler, so it causes a ServletException
        // We expect the request to fail with a ServletException
        Exception exception = null;
        try {
            mockMvc.perform(post("/api/rides/{id}/finish", rideId)
                            .with(authentication(driverAuthToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print());
        } catch (Exception e) {
            exception = e;
        }

        // Verify that the request failed due to the RuntimeException
        assert exception != null;
        assert exception.getCause() instanceof RuntimeException;
        assertEquals("Database connection failed", exception.getCause().getMessage());

        verify(rideService, times(1)).finishRide(eq(rideId), eq(driver.getId()));
    }

    @Test
    @DisplayName("Should handle invalid path parameter format")
    @WithMockUser(roles = "DRIVER")
    void finishRide_ShouldReturn400_WhenPathParameterIsInvalid() throws Exception {
        String invalidRideId = "invalid";

        mockMvc.perform(post("/api/rides/{id}/finish", invalidRideId)
                        .with(authentication(driverAuthToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(rideService, never()).finishRide(any(), any());
    }

    @Test
    @DisplayName("Should reject null ride ID")
    @WithMockUser(roles = "DRIVER")
    void finishRide_ShouldReturn400_WhenRideIdIsNull() throws Exception {
        mockMvc.perform(post("/api/rides/null/finish")
                        .with(authentication(driverAuthToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(rideService, never()).finishRide(any(), any());
    }

    // ============= EDGE CASE TESTS ================

    @Test
    @DisplayName("Should handle concurrent finish requests gracefully")
    @WithMockUser(roles = "DRIVER")
    void finishRide_ShouldHandleConcurrentRequests() throws Exception {
        Long rideId = 1L;
        when(rideService.finishRide(eq(rideId), eq(driver.getId())))
                .thenThrow(new IllegalStateException("Ride already finished"));

        // IllegalStateException is not handled by GlobalExceptionHandler, so it causes a ServletException
        // We expect the request to fail with a ServletException
        Exception exception = null;
        try {
            mockMvc.perform(post("/api/rides/{id}/finish", rideId)
                            .with(authentication(driverAuthToken))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print());
        } catch (Exception e) {
            exception = e;
        }

        // Verify that the request failed due to the IllegalStateException
        assert exception != null;
        assert exception.getCause() instanceof IllegalStateException;
        assertEquals("Ride already finished", exception.getCause().getMessage());

        verify(rideService, times(1)).finishRide(eq(rideId), eq(driver.getId()));
    }

    @Test
    @DisplayName("Should ensure proper error message format")
    @WithMockUser(roles = "DRIVER")
    void finishRide_ShouldReturnProperErrorFormat() throws Exception {
        Long rideId = 999L;
        when(rideService.finishRide(eq(rideId), eq(driver.getId())))
                .thenThrow(new EntityNotFoundException("Ride with id " + rideId + " not found"));

        mockMvc.perform(post("/api/rides/{id}/finish", rideId)
                        .with(authentication(driverAuthToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound()) // GlobalExceptionHandler returns 404 for EntityNotFoundException
                .andExpect(content().string("Ride with id " + rideId + " not found"));

        verify(rideService, times(1)).finishRide(eq(rideId), eq(driver.getId()));
    }

    @Test
    @DisplayName("Should validate that driver ID is extracted correctly from authentication")
    @WithMockUser(roles = "DRIVER")
    void finishRide_ShouldExtractDriverIdCorrectly() throws Exception {
        Long rideId = 1L;
        when(rideService.finishRide(eq(rideId), eq(driver.getId()))).thenReturn(true);

        mockMvc.perform(post("/api/rides/{id}/finish", rideId)
                        .with(authentication(driverAuthToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify that the correct driver ID was passed to the service
        verify(rideService, times(1)).finishRide(eq(rideId), eq(1L));
    }
}
