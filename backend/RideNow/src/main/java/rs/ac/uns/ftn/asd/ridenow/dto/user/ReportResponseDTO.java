package rs.ac.uns.ftn.asd.ridenow.dto.user;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
public class ReportResponseDTO {
    private int sumRides;
    private int avgRides;

    private double sumKM;
    private double avgKM;

    private double sumMoney;
    private double avgMoney;

    private Map<LocalDate, Integer> ridesPerDay;
    private Map<LocalDate, Double> kmPerDay;
    private Map<LocalDate, Double> moneyPerDay;
}
