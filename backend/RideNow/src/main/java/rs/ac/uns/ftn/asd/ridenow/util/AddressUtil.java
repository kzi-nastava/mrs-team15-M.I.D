package rs.ac.uns.ftn.asd.ridenow.util;

import java.util.regex.Pattern;

public class AddressUtil {
    public static String formatAddress(String fullAddress) {
        if (fullAddress == null || fullAddress.trim().isEmpty()) {
            return fullAddress;
        }

        String[] parts = fullAddress.split(",");
        if (parts.length < 1) {
            return fullAddress; // Return original if not enough parts
        }

        StringBuilder formattedAddress = new StringBuilder();

        // Check if first part contains a number (house number)
        String firstPart = parts[0].trim();
        Pattern numberPattern = Pattern.compile("\\d+");
        boolean hasHouseNumber = numberPattern.matcher(firstPart).find();

        if (hasHouseNumber) {
            // If there's a house number, append it and then the street (second part)
            formattedAddress.append(firstPart).append(" ");
            if (parts.length > 1) {
                formattedAddress.append(parts[1].trim());
            }
        } else {
            // If no house number, the first part is the street
            formattedAddress.append(firstPart);
        }

        // Add city (6th from behind, which is parts.length - 6)
        int cityIndex = parts.length - 6;
        if (cityIndex >= 0 && cityIndex < parts.length) {
            formattedAddress.append(", ").append(parts[cityIndex].trim());
        }

        return formattedAddress.toString();
    }
}
