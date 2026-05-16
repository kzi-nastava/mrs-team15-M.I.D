package rs.ac.uns.ftn.asd.ridenow.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BlockUserRequestDTO {

    @NotBlank(message = "Reason is required")
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;

    public BlockUserRequestDTO() {
        super();
    }

    public BlockUserRequestDTO(String reason) {
        this.reason = reason;
    }
}
