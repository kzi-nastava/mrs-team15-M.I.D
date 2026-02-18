package rs.ac.uns.ftn.asd.ridenow.dto.admin;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.dto.model.PriceConfigDTO;

import java.util.List;

@Getter
@Setter
public class PriceConfigRequestDTO {
    @NotEmpty @NotNull
    private List<PriceConfigDTO> prices;
}
