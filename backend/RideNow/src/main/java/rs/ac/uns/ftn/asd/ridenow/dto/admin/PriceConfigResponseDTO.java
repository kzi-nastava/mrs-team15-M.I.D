package rs.ac.uns.ftn.asd.ridenow.dto.admin;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.dto.model.PriceConfigDTO;

import java.util.List;

@Getter
@Setter
public class PriceConfigResponseDTO {
    private List<PriceConfigDTO> prices;
}
