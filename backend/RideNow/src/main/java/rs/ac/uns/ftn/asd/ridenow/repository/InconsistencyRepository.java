package rs.ac.uns.ftn.asd.ridenow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.asd.ridenow.model.Inconsistency;

public interface InconsistencyRepository extends JpaRepository<Inconsistency, Long> {
}
