package rs.ac.uns.ftn.asd.ridenow.dto.user;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

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
