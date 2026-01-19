package rs.ac.uns.ftn.asd.ridenow.dto.driver;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverStatus;

@Getter
@Setter
public class DriverStatusRequestDTO {
    DriverStatus status;
    public DriverStatusRequestDTO(){super();}
}
