package rs.ac.uns.ftn.asd.ridenow.dto.user;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RateRequestDTO {
    @NotNull
    @DecimalMax("5.0")
    @DecimalMin("1.0")
    private double rating;
    private String comment;
}
