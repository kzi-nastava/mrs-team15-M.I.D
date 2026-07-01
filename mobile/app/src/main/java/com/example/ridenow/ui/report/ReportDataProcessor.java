package com.example.ridenow.ui.report;

import com.example.ridenow.dto.report.ReportResponseDTO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

public class ReportDataProcessor {

    private static final SimpleDateFormat DAY_FMT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final long DAY_MS = 24L * 60 * 60 * 1000;

    public static class Result {
        public boolean hasRange;
        public boolean allZero;
        public List<String> labels = new ArrayList<>();
        public List<Double> rides = new ArrayList<>();
        public List<Double> km = new ArrayList<>();
        public List<Double> money = new ArrayList<>();

        public Double ridesSum, ridesAvg, kmSum, kmAvg, moneySum, moneyAvg;
    }

    public static Result process(ReportResponseDTO resp, Long requestedStart, Long requestedEnd) {
        Result result = new Result();
        if (resp == null) return result;

        Map<String, Double> ridesPerDay = resp.getRidesPerDay();
        Map<String, Double> kmPerDay = resp.getKmPerDay();
        Map<String, Double> moneyPerDay = resp.getMoneyPerDay();

        TreeSet<String> labelSet = new TreeSet<>();
        if (ridesPerDay != null) labelSet.addAll(ridesPerDay.keySet());
        if (kmPerDay != null) labelSet.addAll(kmPerDay.keySet());
        if (moneyPerDay != null) labelSet.addAll(moneyPerDay.keySet());

        Long backendStart = null;
        Long backendEnd = null;
        if (resp.getStartDate() != null && resp.getEndDate() != null) {
            try {
                SimpleDateFormat isoFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                Date s = isoFmt.parse(resp.getStartDate());
                Date e = isoFmt.parse(resp.getEndDate());
                if (s != null && e != null) {
                    backendStart = s.getTime();
                    backendEnd = e.getTime();
                }
            } catch (ParseException ignored) { /* fall through to key-derived range */ }
        }

        if ((backendStart == null || backendEnd == null) && !labelSet.isEmpty()) {
            long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
            for (String key : labelSet) {
                try {
                    Date d = DAY_FMT.parse(key);
                    if (d != null) {
                        min = Math.min(min, d.getTime());
                        max = Math.max(max, d.getTime());
                    }
                } catch (ParseException ignored) {}
            }
            if (min != Long.MAX_VALUE) { backendStart = min; backendEnd = max; }
        }

        List<String> labels = new ArrayList<>();
        if (backendStart != null && backendEnd != null) {
            long minTime = requestedStart != null ? Math.max(backendStart, requestedStart) : backendStart;
            long maxTime = requestedEnd != null ? Math.min(backendEnd, requestedEnd) : backendEnd;
            if (minTime <= maxTime) {
                for (long t = minTime; t <= maxTime; t += DAY_MS) {
                    labels.add(DAY_FMT.format(new Date(t)));
                }
            }
        }

        if (labels.isEmpty()) {
            result.hasRange = false;
            result.ridesSum = 0d; result.ridesAvg = 0d;
            result.kmSum = 0d; result.kmAvg = 0d;
            result.moneySum = 0d; result.moneyAvg = 0d;
            return result;
        }

        result.hasRange = true;
        result.labels = labels;

        for (String l : labels) {
            result.rides.add(valueOrZero(ridesPerDay, l));
            result.km.add(valueOrZero(kmPerDay, l));
            result.money.add(valueOrZero(moneyPerDay, l));
        }

        double rawRidesSum = sum(result.rides);
        double rawKmSum = sum(result.km);
        double rawMoneySum = sum(result.money);

        int n = labels.size();
        result.ridesSum = resp.getSumRides() != null ? resp.getSumRides() : round(rawRidesSum, 0);
        result.ridesAvg = resp.getAvgRides() != null ? resp.getAvgRides() : round(result.ridesSum / n, 2);

        result.kmSum = resp.getSumKM() != null ? resp.getSumKM() : round(rawKmSum, 3);
        result.kmAvg = resp.getAvgKM() != null ? resp.getAvgKM() : round(result.kmSum / n, 3);

        result.moneySum = resp.getSumMoney() != null ? resp.getSumMoney() : round(rawMoneySum, 2);
        result.moneyAvg = resp.getAvgMoney() != null ? resp.getAvgMoney() : round(result.moneySum / n, 2);

        result.allZero = allZero(result.rides) && allZero(result.km) && allZero(result.money);
        if (result.allZero) {
            result.ridesSum = null; result.ridesAvg = null;
            result.kmSum = null; result.kmAvg = null;
            result.moneySum = null; result.moneyAvg = null;
        }

        return result;
    }

    private static double valueOrZero(Map<String, Double> map, String key) {
        if (map == null) return 0d;
        Double v = map.get(key);
        return v != null ? v : 0d;
    }

    private static double sum(List<Double> values) {
        double s = 0;
        for (Double v : values) s += v;
        return s;
    }

    private static boolean allZero(List<Double> values) {
        for (Double v : values) if (v != null && v != 0d) return false;
        return true;
    }

    private static double round(double val, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(val * factor) / factor;
    }
}
