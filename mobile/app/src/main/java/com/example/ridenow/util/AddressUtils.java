package com.example.ridenow.util;

public class AddressUtils {

    /**
     * Formats a full address to a shorter format showing only number, street, and city
     * @param fullAddress The full address string
     * @return Formatted address in format "number, street, city"
     */
    public static String formatAddress(String fullAddress) {
        if (fullAddress == null || fullAddress.trim().isEmpty()) {
            return fullAddress;
        }

        // Split the address by commas
        String[] parts = fullAddress.split(",");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }

        if (parts.length < 3) {
            return fullAddress; // Return as-is if format is unexpected
        }

        // Extract: number, street name, and city
        // Typically: "4, Cika Stevina, ..., Novi Sad, ..."
        String number = parts[0];

        // Check if number only contains digits
        boolean isValidNumber = number.matches("^\\d+$");
        if (!isValidNumber) {
            // If first part is not a number, treat it as street name
            String street = parts[0];
            String city = parts.length >= 6 ? parts[parts.length - 6] : parts[parts.length - 1];
            return street + ", " + city;
        }

        String street = parts[1];
        String city = parts.length >= 6 ? parts[parts.length - 6] : parts[parts.length - 1];

        return number + ", " + street + ", " + city;
    }
}
