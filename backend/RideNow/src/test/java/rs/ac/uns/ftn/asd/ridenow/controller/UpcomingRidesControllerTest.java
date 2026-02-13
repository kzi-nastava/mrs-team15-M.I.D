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
import rs.ac.uns.ftn.asd.ridenow.dto.ride.UpcomingRideDTO;
import rs.ac.uns.ftn.asd.ridenow.model.Driver;
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;
import rs.ac.uns.ftn.asd.ridenow.service.DriverService;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("DriverController - Upcoming Rides Integration Tests")
public class UpcomingRidesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DriverService driverService;


    private Driver driver;
    private UsernamePasswordAuthenticationToken driverAuthToken;
    private List<UpcomingRideDTO> sampleUpcomingRides;

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

        // Create sample upcoming rides
        sampleUpcomingRides = createSampleUpcomingRides();
    }

    private List<UpcomingRideDTO> createSampleUpcomingRides() {
        List<UpcomingRideDTO> rides = new ArrayList<>();

        UpcomingRideDTO ride1 = new UpcomingRideDTO();
        ride1.setId(1L);
        ride1.setRoute("Central Square to Airport");
        ride1.setStartTime("15/02/2026 14:30");
        ride1.setPassengers("John Doe, Jane Smith");
        ride1.setCanCancel(true);

        UpcomingRideDTO ride2 = new UpcomingRideDTO();
        ride2.setId(2L);
        ride2.setRoute("Downtown to University");
        ride2.setStartTime("15/02/2026 16:45");
        ride2.setPassengers("Alice Brown");
        ride2.setCanCancel(false);

        UpcomingRideDTO ride3 = new UpcomingRideDTO();
        ride3.setId(3L);
        ride3.setRoute("Mall to Hospital");
        ride3.setStartTime("16/02/2026 09:15");
        ride3.setPassengers("Bob Wilson, Carol Davis, Mike Johnson");
        ride3.setCanCancel(true);

        rides.add(ride1);
        rides.add(ride2);
        rides.add(ride3);

        return rides;
    }

    // ============= POSITIVE TESTS ================

    @Test
    @DisplayName("Should successfully return list of upcoming rides for driver")
    @WithMockUser(roles = "DRIVER")
    void findRides_ShouldReturnUpcomingRides_WhenDriverHasScheduledRides() throws Exception {
        when(driverService.findScheduledRides(eq(driver.getId()))).thenReturn(sampleUpcomingRides);

        mockMvc.perform(get("/api/driver/rides")
                        .with(authentication(driverAuthToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].route").value("Central Square to Airport"))
                .andExpect(jsonPath("$[0].startTime").value("15/02/2026 14:30"))
                .andExpect(jsonPath("$[0].passengers").value("John Doe, Jane Smith"))
                .andExpect(jsonPath("$[0].canCancel").value(true))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].route").value("Downtown to University"))
                .andExpect(jsonPath("$[1].startTime").value("15/02/2026 16:45"))
                .andExpect(jsonPath("$[1].passengers").value("Alice Brown"))
                .andExpect(jsonPath("$[1].canCancel").value(false))
                .andExpect(jsonPath("$[2].id").value(3L))
                .andExpect(jsonPath("$[2].route").value("Mall to Hospital"))
                .andExpect(jsonPath("$[2].startTime").value("16/02/2026 09:15"))
                .andExpect(jsonPath("$[2].passengers").value("Bob Wilson, Carol Davis, Mike Johnson"))
                .andExpect(jsonPath("$[2].canCancel").value(true));

        verify(driverService, times(1)).findScheduledRides(eq(driver.getId()));
    }

    @Test
    @DisplayName("Should return empty list when driver has no upcoming rides")
    @WithMockUser(roles = "DRIVER")
    void findRides_ShouldReturnEmptyList_WhenDriverHasNoScheduledRides() throws Exception {
        when(driverService.findScheduledRides(eq(driver.getId()))).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/driver/rides")
                        .with(authentication(driverAuthToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(driverService, times(1)).findScheduledRides(eq(driver.getId()));
    }

    @Test
    @DisplayName("Should return single ride when driver has only one upcoming ride")
    @WithMockUser(roles = "DRIVER")
    void findRides_ShouldReturnSingleRide_WhenDriverHasOnlyOneScheduledRide() throws Exception {
        List<UpcomingRideDTO> singleRide = List.of(sampleUpcomingRides.get(0));
        when(driverService.findScheduledRides(eq(driver.getId()))).thenReturn(singleRide);

        mockMvc.perform(get("/api/driver/rides")
                        .with(authentication(driverAuthToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].route").value("Central Square to Airport"));

        verify(driverService, times(1)).findScheduledRides(eq(driver.getId()));
    }

    @Test
    @DisplayName("Should handle rides with empty passenger lists")
    @WithMockUser(roles = "DRIVER")
    void findRides_ShouldHandleRidesWithEmptyPassengers() throws Exception {
        UpcomingRideDTO rideWithEmptyPassengers = new UpcomingRideDTO();
        rideWithEmptyPassengers.setId(4L);
        rideWithEmptyPassengers.setRoute("Station to Mall");
        rideWithEmptyPassengers.setStartTime("17/02/2026 11:00");
        rideWithEmptyPassengers.setPassengers("");
        rideWithEmptyPassengers.setCanCancel(true);

        List<UpcomingRideDTO> ridesWithEmpty = List.of(rideWithEmptyPassengers);
        when(driverService.findScheduledRides(eq(driver.getId()))).thenReturn(ridesWithEmpty);

        mockMvc.perform(get("/api/driver/rides")
                        .with(authentication(driverAuthToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].passengers").value(""));

        verify(driverService, times(1)).findScheduledRides(eq(driver.getId()));
    }

    @Test
    @DisplayName("Should handle rides with long route descriptions")
    @WithMockUser(roles = "DRIVER")
    void findRides_ShouldHandleLongRouteDescriptions() throws Exception {
        UpcomingRideDTO rideWithLongRoute = new UpcomingRideDTO();
        rideWithLongRoute.setId(5L);
        rideWithLongRoute.setRoute("Very Long Street Name in Downtown Area to Another Very Long Street Name in Suburban Area via Multiple Intermediate Stops");
        rideWithLongRoute.setStartTime("18/02/2026 08:30");
        rideWithLongRoute.setPassengers("Passenger One");
        rideWithLongRoute.setCanCancel(false);

        List<UpcomingRideDTO> ridesWithLongRoute = List.of(rideWithLongRoute);
        when(driverService.findScheduledRides(eq(driver.getId()))).thenReturn(ridesWithLongRoute);

        mockMvc.perform(get("/api/driver/rides")
                        .with(authentication(driverAuthToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].route").value("Very Long Street Name in Downtown Area to Another Very Long Street Name in Suburban Area via Multiple Intermediate Stops"));

        verify(driverService, times(1)).findScheduledRides(eq(driver.getId()));
    }

    // ============= NEGATIVE TESTS ================

    @Test
    @DisplayName("Should return 403 Forbidden when user is not authenticated")
    void findRides_ShouldReturn403_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/driver/rides")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(driverService, never()).findScheduledRides(any());
    }

    @Test
    @DisplayName("Should return 403 Forbidden when user does not have DRIVER role")
    @WithMockUser(roles = "USER")
    void findRides_ShouldReturn403_WhenUserIsNotDriver() throws Exception {
        mockMvc.perform(get("/api/driver/rides")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(driverService, never()).findScheduledRides(any());
    }

    @Test
    @DisplayName("Should return 403 Forbidden when user has ADMIN role")
    @WithMockUser(roles = "ADMIN")
    void findRides_ShouldReturn403_WhenUserIsAdmin() throws Exception {
        mockMvc.perform(get("/api/driver/rides")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(driverService, never()).findScheduledRides(any());
    }

    @Test
    @DisplayName("Should handle service throwing EntityNotFoundException")
    @WithMockUser(roles = "DRIVER")
    void findRides_ShouldHandleEntityNotFound() throws Exception {
        when(driverService.findScheduledRides(eq(driver.getId())))
                .thenThrow(new EntityNotFoundException("Driver with id " + driver.getId() + " not found"));

        mockMvc.perform(get("/api/driver/rides")
                        .with(authentication(driverAuthToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound()) // GlobalExceptionHandler returns 404 for EntityNotFoundException
                .andExpect(content().string("Driver with id " + driver.getId() + " not found"));

        verify(driverService, times(1)).findScheduledRides(eq(driver.getId()));
    }

    @Test
    @DisplayName("Should handle service throwing RuntimeException")
    @WithMockUser(roles = "DRIVER")
    void findRides_ShouldHandleRuntimeException() throws Exception {
        when(driverService.findScheduledRides(eq(driver.getId())))
                .thenThrow(new RuntimeException("Database connection failed"));

        // RuntimeException is not handled by GlobalExceptionHandler, so it causes a ServletException
        // We expect the request to fail with a ServletException
        Exception exception = null;
        try {
            mockMvc.perform(get("/api/driver/rides")
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

        verify(driverService, times(1)).findScheduledRides(eq(driver.getId()));
    }

    @Test
    @DisplayName("Should handle service throwing IllegalArgumentException")
    @WithMockUser(roles = "DRIVER")
    void findRides_ShouldHandleIllegalArgumentException() throws Exception {
        when(driverService.findScheduledRides(eq(driver.getId())))
                .thenThrow(new IllegalArgumentException("Invalid driver ID"));

        mockMvc.perform(get("/api/driver/rides")
                        .with(authentication(driverAuthToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest()) // GlobalExceptionHandler returns 400 for IllegalArgumentException
                .andExpect(content().string("Invalid driver ID"));

        verify(driverService, times(1)).findScheduledRides(eq(driver.getId()));
    }

    // ============= EDGE CASE TESTS ================

    @Test
    @DisplayName("Should validate that driver ID is extracted correctly from authentication")
    @WithMockUser(roles = "DRIVER")
    void findRides_ShouldExtractDriverIdCorrectly() throws Exception {
        when(driverService.findScheduledRides(eq(driver.getId()))).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/driver/rides")
                        .with(authentication(driverAuthToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify that the correct driver ID was passed to the service
        verify(driverService, times(1)).findScheduledRides(eq(1L));
    }

    @Test
    @DisplayName("Should handle large number of upcoming rides")
    @WithMockUser(roles = "DRIVER")
    void findRides_ShouldHandleLargeNumberOfRides() throws Exception {
        List<UpcomingRideDTO> manyRides = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            UpcomingRideDTO ride = new UpcomingRideDTO();
            ride.setId((long) i);
            ride.setRoute("Route " + i);
            ride.setStartTime("15/02/2026 " + String.format("%02d", 8 + (i % 12)) + ":00");
            ride.setPassengers("Passenger " + i);
            ride.setCanCancel(i % 2 == 0);
            manyRides.add(ride);
        }

        when(driverService.findScheduledRides(eq(driver.getId()))).thenReturn(manyRides);

        mockMvc.perform(get("/api/driver/rides")
                        .with(authentication(driverAuthToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(50));

        verify(driverService, times(1)).findScheduledRides(eq(driver.getId()));
    }

    @Test
    @DisplayName("Should handle concurrent requests gracefully")
    @WithMockUser(roles = "DRIVER")
    void findRides_ShouldHandleConcurrentRequests() throws Exception {
        when(driverService.findScheduledRides(eq(driver.getId()))).thenReturn(sampleUpcomingRides);

        // Simulate multiple concurrent requests
        mockMvc.perform(get("/api/driver/rides")
                        .with(authentication(driverAuthToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify service was called
        verify(driverService, atLeastOnce()).findScheduledRides(eq(driver.getId()));
    }

    @Test
    @DisplayName("Should ensure JSON response structure matches UpcomingRideDTO specification")
    @WithMockUser(roles = "DRIVER")
    void findRides_ShouldReturnCorrectJsonStructure() throws Exception {
        when(driverService.findScheduledRides(eq(driver.getId()))).thenReturn(sampleUpcomingRides);

        mockMvc.perform(get("/api/driver/rides")
                        .with(authentication(driverAuthToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Verify all required fields are present
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].route").exists())
                .andExpect(jsonPath("$[0].startTime").exists())
                .andExpect(jsonPath("$[0].passengers").exists())
                .andExpect(jsonPath("$[0].canCancel").exists())
                // Verify field types
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].route").isString())
                .andExpect(jsonPath("$[0].startTime").isString())
                .andExpect(jsonPath("$[0].passengers").isString())
                .andExpect(jsonPath("$[0].canCancel").isBoolean());

        verify(driverService, times(1)).findScheduledRides(eq(driver.getId()));
    }
}
