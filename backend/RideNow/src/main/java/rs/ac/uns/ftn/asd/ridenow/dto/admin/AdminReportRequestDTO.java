package rs.ac.uns.ftn.asd.ridenow.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
public class AdminReportRequestDTO {
    @Min(0)
    private Long startDate;
    @Min(0)
    private Long endDate;

    @NotNull
    private boolean drivers;
    @NotNull
    private boolean users;
    @NotNull
    private String personId;

}
