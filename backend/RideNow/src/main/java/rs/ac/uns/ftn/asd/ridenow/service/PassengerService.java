package rs.ac.uns.ftn.asd.ridenow.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RouteResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.RateRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.RateResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.Rating;
import rs.ac.uns.ftn.asd.ridenow.model.Ride;
import rs.ac.uns.ftn.asd.ridenow.repository.RatingRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.RideRepository;

import java.time.LocalDateTime;

@Service
public class PassengerService {

    private final RatingRepository ratingRepository;
    private final RideRepository rideRepository;

    public PassengerService(RatingRepository ratingRepository, RideRepository rideRepository) {
        this.rideRepository = rideRepository;
        this.ratingRepository = ratingRepository;
    }

    public RouteResponseDTO addToFavorites(Long userId, Long routeId) {

        RouteResponseDTO dto = new RouteResponseDTO();
        dto.setRouteId(routeId);
        dto.setDistanceKm(14.0);
        dto.setEstimatedTimeMinutes(25);
        dto.setPriceEstimate(1800);

        return dto;
    }

    public RateResponseDTO makeRating(RateRequestDTO req, Long userId) {
        Ride ride = rideRepository.findById(req.getRideId())
                .orElseThrow(() -> new EntityNotFoundException("Ride not found"));

        // Create and populate the rating entity
        Rating rating = new Rating();
        rating.setRide(ride);
        rating.setVehicleRating(req.getVehicleRating());
        rating.setDriverRating(req.getDriverRating());
        rating.setDriverComment(req.getDriverComment());
        rating.setVehicleComment(req.getVehicleComment());
        rating.setCreatedAt(LocalDateTime.now());

        // Save to database
        Rating savedRating = ratingRepository.save(rating);
        return(new RateResponseDTO(savedRating));
    }

    public void removeFromFavorites(Long userId, Long routeId) {
        // mock: route removed from favorites
    }
}
