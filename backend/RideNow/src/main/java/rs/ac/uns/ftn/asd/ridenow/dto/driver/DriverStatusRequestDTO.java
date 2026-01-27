package rs.ac.uns.ftn.asd.ridenow.dto.driver;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverStatus;

@Getter
@Setter
public class DriverStatusRequestDTO {

    @NotNull(message = "Driver status is required")
    DriverStatus status;

    public DriverStatusRequestDTO(){super();}
}
