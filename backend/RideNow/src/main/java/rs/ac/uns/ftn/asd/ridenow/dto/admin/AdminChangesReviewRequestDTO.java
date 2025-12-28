package rs.ac.uns.ftn.asd.ridenow.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AdminChangesReviewRequestDTO {
    private boolean approved;
    private String message;

    public AdminChangesReviewRequestDTO() { super(); }
}
