package rs.ac.uns.ftn.asd.ridenow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.asd.ridenow.model.Rating;
import rs.ac.uns.ftn.asd.ridenow.model.Ride;

public interface RatingRepository extends JpaRepository<Rating, Integer> {
    Rating findByRide(Ride ride);
}
