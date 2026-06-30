package com.example.ridenow.dto.report;

import java.util.Map;

public class ReportResponseDTO {

    private String startDate;
    private String endDate;

    private Map<String, Double> ridesPerDay;
    private Map<String, Double> kmPerDay;
    private Map<String, Double> moneyPerDay;

    private Double sumRides;
    private Double avgRides;
    private Double sumKM;
    private Double avgKM;
    private Double sumMoney;
    private Double avgMoney;

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public Map<String, Double> getRidesPerDay() { return ridesPerDay; }
    public void setRidesPerDay(Map<String, Double> ridesPerDay) { this.ridesPerDay = ridesPerDay; }

    public Map<String, Double> getKmPerDay() { return kmPerDay; }
    public void setKmPerDay(Map<String, Double> kmPerDay) { this.kmPerDay = kmPerDay; }

    public Map<String, Double> getMoneyPerDay() { return moneyPerDay; }
    public void setMoneyPerDay(Map<String, Double> moneyPerDay) { this.moneyPerDay = moneyPerDay; }

    public Double getSumRides() { return sumRides; }
    public void setSumRides(Double sumRides) { this.sumRides = sumRides; }

    public Double getAvgRides() { return avgRides; }
    public void setAvgRides(Double avgRides) { this.avgRides = avgRides; }

    public Double getSumKM() { return sumKM; }
    public void setSumKM(Double sumKM) { this.sumKM = sumKM; }

    public Double getAvgKM() { return avgKM; }
    public void setAvgKM(Double avgKM) { this.avgKM = avgKM; }

    public Double getSumMoney() { return sumMoney; }
    public void setSumMoney(Double sumMoney) { this.sumMoney = sumMoney; }

    public Double getAvgMoney() { return avgMoney; }
    public void setAvgMoney(Double avgMoney) { this.avgMoney = avgMoney; }
}
